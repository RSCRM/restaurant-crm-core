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
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.identity.dto.request.ContextSelectionRequest;
import com.restaurant.crm.modules.identity.dto.response.ContextResponse;
import com.restaurant.crm.modules.identity.dto.response.ContextSelectionResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final String TOKEN_TYPE_IDENTITY = "IDENTITY";
    private static final String TOKEN_TYPE_CONTEXT = "CONTEXT";
    private static final String OWNER_ROLE = "OWNER";
    private static final long IDENTITY_TOKEN_EXPIRY_MINUTES = 15;
    private static final long CONTEXT_TOKEN_EXPIRY_HOURS = 72;

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    EmployeeRepository employeeRepository;
    OrganizationRepository organizationRepository;
    OrganizationBranchRepository organizationBranchRepository;
    OrgRoleRepository orgRoleRepository;
    RoleRepository roleRepository;
    RedisBlacklistRepository redisBlacklistRepository;

    @NonFinal
    @Value(value = "${security.jwt.signer-key}")
    String SIGNER_KEY;

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_USERNAME_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }

        // Load employee contexts
        List<Employee> employees = employeeRepository.findByUserIdAndStatus(
                user.getId(), EmployeeStatus.ACTIVE);

        List<ContextResponse> contexts = new java.util.ArrayList<>(employees.stream()
                .map(this::buildContextResponse)
                .toList());

        // Load owner context (user is owner of an organization)
        organizationRepository.findByOwnerId(user.getId())
                .ifPresent(organization -> contexts.addAll(
                        organizationBranchRepository
                                .findByOrganizationIdAndStatus(organization.getId(), OrganizationBranchStatus.ACTIVE)
                                .stream()
                                .map(branch -> buildOwnerContextResponse(organization, branch))
                                .toList()));

        // System roles from User.roles (identity module roles)
        Set<String> systemRoles = buildSystemRoles(user);

        // Generate Identity Token
        String identityToken = generateIdentityToken(user);

        return AuthenticationResponse.builder()
                .accessToken(identityToken)
                .refreshToken(null)
                .contexts(contexts)
                .systemRoles(systemRoles)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ContextSelectionResponse selectContext(ContextSelectionRequest request, String identityToken) {
        // Parse and verify Identity Token
        SignedJWT signedJWT = parseAndVerifyToken(identityToken);

        String tokenType = extractClaim(signedJWT, JwtClaimSetConstant.CLAIM_TYPE);
        if (!TOKEN_TYPE_IDENTITY.equals(tokenType)) {
            throw new AppException(ErrorCode.AUTH_INVALID_TOKEN_TYPE);
        }

        String userId = extractClaim(signedJWT, JwtClaimSetConstant.CLAIM_USER_ID);

        // Owner path: no employeeId, just organizationId
        if (request.getEmployeeId() == null) {
            if (request.getBranchId() == null || request.getBranchId().isBlank()) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            OrganizationBranch branch = organizationBranchRepository
                    .findByIdAndOrganization_OwnerId(request.getBranchId(), userId)
                    .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));
            if (!branch.getOrganization().getId().equals(request.getOrganizationId())) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            String contextToken = generateOwnerContextToken(userId, request.getOrganizationId(), branch.getId());
            return ContextSelectionResponse.builder()
                    .contextToken(contextToken)
                    .build();
        }

        // Employee path: verify employee belongs to user
        Employee employee = employeeRepository.findByIdAndUserId(request.getEmployeeId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_ACTIVE);
        }

        // Load permissions from OrgRole
        Set<String> permissions = buildOrgPermissions(employee);

        if (employee.getOrgRole() == null) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        String roleName = employee.getOrgRole().getRoleName();

        // Generate Context Token
        String contextToken = generateContextToken(
                userId, employee, roleName, employee.getOrgRole().getDataScope(), permissions);

        return ContextSelectionResponse.builder()
                .contextToken(contextToken)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            SignedJWT signedJWT = parseAndVerifyToken(request.getToken());
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isValid = expirationTime.after(new Date());
            return IntrospectResponse.builder()
                    .isValid(isValid)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Introspect token failed", e);
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
    }

    private String generateIdentityToken(User user) {
        return getJwsHeader(user);
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


    private String getJwsHeader(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(IDENTITY_TOKEN_EXPIRY_MINUTES, ChronoUnit.DAYS)))
                .jwtID(UUID.randomUUID().toString())
                .claim(JwtClaimSetConstant.CLAIM_USER_ID, user.getId())
                .claim(JwtClaimSetConstant.CLAIM_TYPE, TOKEN_TYPE_IDENTITY)
                .claim(JwtClaimSetConstant.CLAIM_SCOPE, buildSystemRoles(user))
                .build();

        return signToken(jwsHeader, jwtClaimsSet);
    }

    private String generateContextToken(
            String userId,
            Employee employee,
            String roleName,
            OrgDataScope dataScope,
            Set<String> permissions) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        OrganizationBranch branch = employee.getBranch();
        String branchId = branch != null ? branch.getId() : null;
        String organizationId = branch != null && branch.getOrganization() != null
                ? branch.getOrganization().getId() : null;

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(employee.getEmail())
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(CONTEXT_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS)))
                .jwtID(UUID.randomUUID().toString())
                .claim(JwtClaimSetConstant.CLAIM_USER_ID, userId)
                .claim(JwtClaimSetConstant.CLAIM_TYPE, TOKEN_TYPE_CONTEXT)
                .claim(JwtClaimSetConstant.CLAIM_EMPLOYEE_ID, employee.getId())
                .claim(JwtClaimSetConstant.CLAIM_ORG_ROLE, roleName)
                .claim(JwtClaimSetConstant.CLAIM_DATA_SCOPE, dataScope.name())
                .claim(JwtClaimSetConstant.CLAIM_PERMISSION, permissions);

        if (organizationId != null) {
            claimsBuilder.claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, organizationId);
        }
        if (branchId != null) {
            claimsBuilder.claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, branchId);
        }

        return signToken(jwsHeader, claimsBuilder.build());
    }

    private String generateOwnerContextToken(String userId, String organizationId, String branchId) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        Set<String> permissions = orgRoleRepository.findByRoleName(OWNER_ROLE)
                .map(this::buildOrgPermissions)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(CONTEXT_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS)))
                .jwtID(UUID.randomUUID().toString())
                .claim(JwtClaimSetConstant.CLAIM_USER_ID, userId)
                .claim(JwtClaimSetConstant.CLAIM_TYPE, TOKEN_TYPE_CONTEXT)
                .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, organizationId)
                .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, branchId)
                .claim(JwtClaimSetConstant.CLAIM_ORG_ROLE, OWNER_ROLE)
                .claim(JwtClaimSetConstant.CLAIM_DATA_SCOPE, OrgDataScope.ORGANIZATION.name())
                .claim(JwtClaimSetConstant.CLAIM_PERMISSION, permissions)
                .build();

        return signToken(jwsHeader, jwtClaimsSet);
    }

    private String signToken(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet) {
        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwtClaimsSet.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.AUTH_GENERATION_FAIL);
        }
    }

    // ==================== Token Parsing ====================

    private SignedJWT parseAndVerifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            boolean verified = signedJWT.verify(verifier);
            if (!verified) {
                throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
            }
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime.before(new Date())) {
                throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
            }
            return signedJWT;
        } catch (ParseException e) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
    }

    private String extractClaim(SignedJWT signedJWT, String claimName) {
        try {
            Object claim = signedJWT.getJWTClaimsSet().getClaim(claimName);
            return claim != null ? claim.toString() : null;
        } catch (ParseException e) {
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }
    }

    private ContextResponse buildContextResponse(Employee employee) {
        OrganizationBranch branch = employee.getBranch();
        String branchId = branch != null ? branch.getId() : null;
        String branchName = branch != null ? branch.getBranchName() : null;
        String organizationId = branch != null && branch.getOrganization() != null
                ? branch.getOrganization().getId() : null;
        String organizationName = branch != null && branch.getOrganization() != null
                ? branch.getOrganization().getOrganizationName() : null;
        String roleName = employee.getOrgRole() != null ? employee.getOrgRole().getRoleName() : null;

        return ContextResponse.builder()
                .employeeId(employee.getId())
                .organizationId(organizationId)
                .organizationName(organizationName)
                .branchId(branchId)
                .branchName(branchName)
                .role(roleName)
                .build();
    }

    private ContextResponse buildOwnerContextResponse(
            Organization organization,
            OrganizationBranch branch) {
        return ContextResponse.builder()
                .employeeId(null)
                .organizationId(organization.getId())
                .organizationName(organization.getOrganizationName())
                .branchId(branch.getId())
                .branchName(branch.getBranchName())
                .role("OWNER")
                .build();
    }

    private Set<String> buildSystemRoles(User user) {
        Set<String> roles = new HashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> roles.add(role.getRoleName()));
        }
        return roles;
    }

    private Set<String> buildOrgPermissions(Employee employee) {
        return buildOrgPermissions(employee.getOrgRole());
    }

    private Set<String> buildOrgPermissions(OrgRole orgRole) {
        Set<String> permissions = new HashSet<>();
        if (orgRole != null && orgRole.getOrgPermissions() != null) {
            orgRole.getOrgPermissions()
                    .forEach(permission -> permissions.add(permission.getPermissionName()));
        }
        return permissions;
    }
}
