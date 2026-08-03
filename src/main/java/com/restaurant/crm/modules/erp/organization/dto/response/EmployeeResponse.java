package com.restaurant.crm.modules.erp.organization.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    String id;
    String username;
    String email;
    String phone;
    String branchId;
    String orgRoleName;
    BigDecimal salary;
    String status;
    LocalDate startDate;
    boolean profileUpdateEnabled;
}
