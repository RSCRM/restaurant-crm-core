package com.restaurant.crm.modules.erp.manage_branch_manager.dto.request;

import com.restaurant.crm.modules.erp.shared.constants.employee.EmployeeConstants;
import com.restaurant.crm.modules.identity.constants.user.UserConstants;
import com.restaurant.crm.modules.identity.constants.user.UserErrorCodeConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BranchManagerCreationRequest {

    @NotBlank(message = UserErrorCodeConstants.USER_USERNAME_INVALID)
    @Size(min = UserConstants.MIN_CHARS_USERNAME,
            max = UserConstants.MAX_CHARS_USERNAME,
            message = UserErrorCodeConstants.USER_USERNAME_INVALID)
    String username;

    @NotBlank(message = UserErrorCodeConstants.USER_PASSWORD_INVALID)
    @Size(min = UserConstants.MIN_CHARS_PASSWORD,
            max = UserConstants.MAX_CHARS_PASSWORD,
            message = UserErrorCodeConstants.USER_PASSWORD_INVALID)
    String password;

    @NotBlank(message = "BRANCH_MANAGER_INVALID_REQUEST")
    @Email(message = "BRANCH_MANAGER_INVALID_REQUEST")
    String email;

    @NotBlank(message = "BRANCH_MANAGER_INVALID_REQUEST")
    @Size(min = EmployeeConstants.MIN_CHARS_FULL_NAME,
            max = EmployeeConstants.MAX_CHARS_FULL_NAME,
            message = "BRANCH_MANAGER_INVALID_REQUEST")
    String fullName;

    String phone;

    @NotBlank(message = "BRANCH_MANAGER_INVALID_REQUEST")
    String branchId;
}
