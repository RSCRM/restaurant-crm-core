package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeErrorCodeConstants;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.identity.constants.user.UserConstants;
import com.restaurant.crm.modules.identity.constants.user.UserErrorCodeConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateEmployeeRequest {

    @NotBlank(message = EmployeeErrorCodeConstants.EMPLOYEE_FIRST_NAME_REQUIRED)
    @Size(max = EmployeeConstants.MAX_CHARS_NAME,
            message = EmployeeErrorCodeConstants.EMPLOYEE_FIRST_NAME_INVALID)
    String firstName;

    @NotBlank(message = EmployeeErrorCodeConstants.EMPLOYEE_LAST_NAME_REQUIRED)
    @Size(max = EmployeeConstants.MAX_CHARS_NAME,
            message = EmployeeErrorCodeConstants.EMPLOYEE_LAST_NAME_INVALID)
    String lastName;

    @NotBlank(message = UserErrorCodeConstants.USER_USERNAME_INVALID)
    @Size(min = UserConstants.MIN_CHARS_USERNAME,
            max = UserConstants.MAX_CHARS_USERNAME,
            message = UserErrorCodeConstants.USER_USERNAME_INVALID)
    String username;

    @NotBlank(message = EmployeeErrorCodeConstants.EMPLOYEE_EMAIL_REQUIRED)
    @Email(message = EmployeeErrorCodeConstants.EMPLOYEE_EMAIL_INVALID)
    @Size(max = EmployeeConstants.MAX_CHARS_EMAIL,
            message = EmployeeErrorCodeConstants.EMPLOYEE_EMAIL_INVALID)
    String email;

    @Size(max = EmployeeConstants.MAX_CHARS_PHONE,
            message = EmployeeErrorCodeConstants.EMPLOYEE_PHONE_INVALID)
    String phone;

    @NotBlank(message = EmployeeErrorCodeConstants.EMPLOYEE_BRANCH_REQUIRED)
    String branchId;

    String orgRoleId;

    String orgRoleName;

    String role;

    @NotNull(message = EmployeeErrorCodeConstants.EMPLOYEE_STATUS_REQUIRED)
    EmployeeStatus status;

    @NotNull(message = EmployeeErrorCodeConstants.EMPLOYEE_START_DATE_REQUIRED)
    LocalDate startDate;

    LocalDate endDate;

    @NotBlank(message = UserErrorCodeConstants.USER_PASSWORD_INVALID)
    @Size(min = UserConstants.MIN_CHARS_PASSWORD,
            max = UserConstants.MAX_CHARS_PASSWORD,
            message = UserErrorCodeConstants.USER_PASSWORD_INVALID)
    String password;
}
