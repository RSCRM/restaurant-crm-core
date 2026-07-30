package com.restaurant.crm.modules.erp.organization.dto.response;

import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    String id;
    String employeeId;
    String employeeCode;
    String firstName;
    String lastName;
    String fullName;
    String userId;
    String username;
    String email;
    String phone;
    String organizationId;
    String branchId;
    String branchName;
    String orgRoleId;
    String orgRoleName;
    String role;
    EmployeeStatus status;
    boolean enabled;
    UserStatus userStatus;
    LocalDate startDate;
    LocalDate endDate;
    Instant createdAt;
    Instant updatedAt;
}
