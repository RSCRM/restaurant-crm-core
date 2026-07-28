package com.restaurant.crm.modules.profile.dto.request;

import com.restaurant.crm.modules.profile.constants.UserProfileConstants;
import com.restaurant.crm.modules.profile.constants.UserProfileErrorCodeConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffProfileUpdateRequest {

    @Size(min = UserProfileConstants.MIN_CHARS_FULL_NAME,
            max = UserProfileConstants.MAX_CHARS_FULL_NAME,
            message = UserProfileErrorCodeConstants.USER_FULL_NAME_INVALID)
    String fullName;

    @Pattern(regexp = UserProfileConstants.PHONE_PATTERN,
            message = UserProfileErrorCodeConstants.USER_PHONE_INVALID)
    String phone;

    @Email(message = "EMPLOYEE_EMAIL_INVALID")
    String email;
}
