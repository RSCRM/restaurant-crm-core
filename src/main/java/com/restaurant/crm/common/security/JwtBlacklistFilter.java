package com.restaurant.crm.common.security;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.redis.RedisBlacklistRepository;
import com.restaurant.crm.common.redis.RedisKeyGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JwtBlacklistFilter extends OncePerRequestFilter {
    RedisBlacklistRepository redisBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String tokenHash = RedisKeyGenerator.generateBlacklistKey(token);

            if (redisBlacklistRepository.exists(tokenHash)) {
                log.warn("Revoked token detected.");
                throw new AppException(ErrorCode.AUTH_TOKEN_REVOKED);
            }
        }

        filterChain.doFilter(request, response);
    }
}
