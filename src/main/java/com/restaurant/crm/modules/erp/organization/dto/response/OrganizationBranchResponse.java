package com.restaurant.crm.modules.erp.organization.dto.response;


import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
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
public class OrganizationBranchResponse {
    String id;
    String organizationId;
    String managerId;
    String managerUserId;
    String managerName;
    String managerUsername;
    String managerEmail;
    String branchName;
    String address;
    String phone;
    OrganizationBranchStatus status;
    Instant createdAt;
    Instant updatedAt;
}
