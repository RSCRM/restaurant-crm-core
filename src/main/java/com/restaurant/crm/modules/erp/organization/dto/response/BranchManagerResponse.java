package com.restaurant.crm.modules.erp.organization.dto.response;

import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BranchManagerResponse {
    String employeeId;
    String userId;
    String username;
    String email;
    boolean enabled;
    String phone;
    EmployeeStatus status;
    LocalDate startDate;
    LocalDate endDate;
    String branchId;
    String branchName;
    String orgRoleId;
    String orgRoleName;
}
