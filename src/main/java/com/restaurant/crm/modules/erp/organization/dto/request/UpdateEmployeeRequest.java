package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.profile.constants.UserProfileConstants;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEmployeeRequest {
    @Size(min = UserProfileConstants.MIN_CHARS_FULL_NAME,
            max = UserProfileConstants.MAX_CHARS_FULL_NAME,
            message = "EMPLOYEE_FULL_NAME_INVALID")
    String fullName;

    @Pattern(regexp = UserProfileConstants.PHONE_PATTERN, message = "EMPLOYEE_PHONE_INVALID")
    String phone;

    EmployeeStatus status;

    LocalDate startDate;

    LocalDate endDate;
}
