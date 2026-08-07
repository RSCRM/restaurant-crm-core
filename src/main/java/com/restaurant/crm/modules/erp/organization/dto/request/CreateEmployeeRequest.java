package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.identity.constants.user.UserConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "USER_FULL_NAME_INVALID")
    String firstName;

    @NotBlank(message = "USER_FULL_NAME_INVALID")
    String lastName;

    @NotBlank(message = "EMPLOYEE_USERNAME_REQUIRED")
    @Size(
            min = UserConstants.MIN_CHARS_USERNAME,
            max = UserConstants.MAX_CHARS_USERNAME,
            message = "EMPLOYEE_USERNAME_REQUIRED"
    )
    String username;

    @NotBlank(message = "USER_PASSWORD_INVALID")
    @Size(
            min = UserConstants.MIN_CHARS_PASSWORD,
            max = UserConstants.MAX_CHARS_PASSWORD,
            message = "USER_PASSWORD_INVALID"
    )
    String password;

    @NotBlank(message = "EMPLOYEE_EMAIL_REQUIRED")
    @Email(message = "EMPLOYEE_EMAIL_INVALID")
    String email;

    @NotBlank(message = "EMPLOYEE_PHONE_REQUIRED")
    String phone;

    String branchId;

    @NotBlank(message = "EMPLOYEE_ORG_ROLE_REQUIRED")
    String orgRoleId;

    @NotNull(message = "EMPLOYEE_START_DATE_REQUIRED")
    LocalDate startDate;

    @PositiveOrZero(message = "EMPLOYEE_SALARY_INVALID")
    BigDecimal salary;

    EmployeeStatus status;

    LocalDate endDate;
}
