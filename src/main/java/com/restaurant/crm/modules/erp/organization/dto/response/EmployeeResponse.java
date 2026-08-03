package com.restaurant.crm.modules.erp.organization.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
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
    BigDecimal salary;
    String status;
    boolean enabled;
    String userStatus;
    LocalDate startDate;
    LocalDate endDate;
    Instant createdAt;
    Instant updatedAt;
    boolean profileUpdateEnabled;
}
