package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;

import java.time.Instant;


public interface CustomerSessionTokenService {


    IssuedToken issue(String sessionId,
                      String deviceId,
                      String organizationId,
                      String branchId,
                      String tableId,
                      SessionMemberRole role,
                      long ttlSeconds);


    record IssuedToken(String token, Instant expiresAt) {
    }
}
