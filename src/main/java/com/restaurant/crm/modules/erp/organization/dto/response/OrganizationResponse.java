package com.restaurant.crm.modules.erp.organization.dto.response;

import com.restaurant.crm.modules.erp.organization.enums.OrganizationStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationResponse {
    String id;
    String ownerId;
    String organizationName;
    String taxCode;
    String address;
    String phone;
    String email;
    OrganizationStatus status;
    Instant createdAt;
    Instant updatedAt;
}
