package com.restaurant.crm.modules.erp.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class CreateEmployeeRequest {

    @NotBlank(message = "EMPLOYEE_USERNAME_REQUIRED")
    String username;

    @NotBlank(message = "EMPLOYEE_EMAIL_REQUIRED")
    @Email(message = "EMPLOYEE_EMAIL_INVALID")
    String email;

    @NotBlank(message = "EMPLOYEE_PHONE_REQUIRED")
    String phone;

    @NotBlank(message = "EMPLOYEE_BRANCH_REQUIRED")
    String branchId;

    String orgRoleId;

    @NotNull(message = "EMPLOYEE_START_DATE_REQUIRED")
    LocalDate startDate;

    @PositiveOrZero(message = "EMPLOYEE_SALARY_INVALID")
    BigDecimal salary;
}
