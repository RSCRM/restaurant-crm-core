package com.restaurant.crm.modules.identity.utils;

import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


@Slf4j
public class AuthUtils {

    private AuthUtils() {}

    public static String getCurrentUserId()  {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object claim = jwt.getClaims().get(JwtClaimSetConstant.CLAIM_USER_ID);
            log.info("Claim userId: {}", claim.toString());
            return claim.toString();
        } else  {
            log.info("Cannot get current user id from jwt");
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }
    }

    public static String getCurrentUserName()  {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        } else {
            log.info("Cannot get current user name from context");
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }
    }
}
