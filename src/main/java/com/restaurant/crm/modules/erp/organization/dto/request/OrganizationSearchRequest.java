package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.organization.enums.OrganizationStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationSearchRequest {
    // Search
    String organizationName;
    String taxCode;
    String phone;
    String email;

    // Filter
    String ownerId;
    OrganizationStatus status;
    String address;
}
