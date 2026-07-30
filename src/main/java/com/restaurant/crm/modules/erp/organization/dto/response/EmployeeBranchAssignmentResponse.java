package com.restaurant.crm.modules.erp.organization.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeBranchAssignmentResponse {
    String branchId;
    String branchName;
    String branchAddress;
    String branchPhone;
    String branchStatus;
    String managerId;
    String managerName;
    EmployeeResponse manager;
}
