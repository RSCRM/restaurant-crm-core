package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.profile.constants.UserProfileConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "EMPLOYEE_USERNAME_REQUIRED")
    String username;

    @NotBlank(message = "EMPLOYEE_EMAIL_REQUIRED")
    @Email(message = "EMPLOYEE_EMAIL_INVALID")
    String email;

    @NotBlank(message = "EMPLOYEE_FULL_NAME_REQUIRED")
    @Size(min = UserProfileConstants.MIN_CHARS_FULL_NAME,
            max = UserProfileConstants.MAX_CHARS_FULL_NAME,
            message = "EMPLOYEE_FULL_NAME_INVALID")
    String fullName;

    @Pattern(regexp = UserProfileConstants.PHONE_PATTERN, message = "EMPLOYEE_PHONE_INVALID")
    String phone;

    @NotBlank(message = "EMPLOYEE_BRANCH_REQUIRED")
    String branchId;

    @NotNull(message = "EMPLOYEE_START_DATE_REQUIRED")
    LocalDate startDate;

    @PositiveOrZero(message = "EMPLOYEE_SALARY_INVALID")
    BigDecimal salary;
}
