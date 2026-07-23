package com.restaurant.crm.modules.identity.service.impl;

import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.redis.RedisBlacklistRepository;
import com.restaurant.crm.common.redis.RedisKeyGenerator;
import com.restaurant.crm.modules.identity.dto.request.AuthenticationRequest;
import com.restaurant.crm.modules.identity.dto.request.IntrospectRequest;
import com.restaurant.crm.modules.identity.dto.response.AuthenticationResponse;
import com.restaurant.crm.modules.identity.dto.response.IntrospectResponse;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.service.interfaces.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    RedisBlacklistRepository redisBlacklistRepository;

    @NonFinal
    @Value(value = "${security.jwt.signer-key}")
    String SIGNER_KEY;

    // authenticate
    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String login = request.getUsername();

        User user = userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login))
                .orElseThrow(() -> new AppException(ErrorCode.USER_USERNAME_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
        String token = generateToken(user);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .roles(buildRoles(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean expirationTimeValidated = expirationTime.after(new Date());
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            boolean verified = signedJWT.verify(verifier);
            return IntrospectResponse.builder()
                    .isValid(expirationTimeValidated && verified)
                    .build();
        } catch (ParseException e) {
            log.error("Parse token failed", e);
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            log.error("Verify token failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logout(String token) {
        try {
            // parse & verify JWT signature
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            boolean verified = signedJWT.verify(verifier);
            if (!verified) {
                throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
            }

            // check expiration
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime.before(new Date())) {
                throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
            }

            // calculate remaining TTL
            long remainingTtlInSeconds = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;

            // hash token & save to Redis blacklist
            String tokenHash = RedisKeyGenerator.generateBlacklistKey(token);
            redisBlacklistRepository.save(tokenHash, remainingTtlInSeconds);

            log.info("User {} logout successfully.", signedJWT.getJWTClaimsSet().getSubject());
        } catch (ParseException e) {
            log.error("Parse token failed", e);
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        } catch (JOSEException e) {
            log.error("Verify token failed", e);
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
    }

    private String generateToken(User user) {
        // header
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        // payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issueTime(new Date())
                .expirationTime(
                        Date.from(Instant.now().plus(72, ChronoUnit.HOURS)))
                .jwtID(UUID.randomUUID().toString())
                .claim(JwtClaimSetConstant.CLAIM_USER_ID, user.getId())
                .claim(JwtClaimSetConstant.CLAIM_SCOPE, buildScope(user))
                .claim(JwtClaimSetConstant.CLAIM_PERMISSION, buildPermission(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // build JsonObject
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        // signer
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.AUTH_GENERATION_FAIL);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        boolean isEmpty = CollectionUtils.isEmpty(user.getRoles());
        if (!isEmpty) {
            user.getRoles().forEach(role -> stringJoiner.add(role.getRoleName()));
        }
        return stringJoiner.toString();
    }

    private Set<String> buildRoles(User user) {
        HashSet<String> roles = new HashSet<>();
        boolean isEmpty = CollectionUtils.isEmpty(user.getRoles());

        if (!isEmpty) {
            user.getRoles().forEach(
                    role -> roles.add(role.getRoleName()));
        }

        return roles;
    }

    private Set<String> buildPermission(User user) {
        HashSet<String> permissions = new HashSet<>();
        boolean isEmpty = CollectionUtils.isEmpty(user.getRoles());

        if (!isEmpty) {
            user.getRoles().forEach(
                    role -> role.getPermissions().forEach(
                            permission -> permissions.add(permission.getPermissionName())));
        }

        return permissions;
    }

}
