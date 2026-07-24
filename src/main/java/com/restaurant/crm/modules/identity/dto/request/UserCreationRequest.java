package com.restaurant.crm.modules.identity.dto.request;

import com.restaurant.crm.modules.identity.constants.user.UserConstants;
import com.restaurant.crm.modules.identity.constants.user.UserErrorCodeConstants;
import com.restaurant.crm.modules.profile.constants.UserProfileConstants;
import com.restaurant.crm.modules.profile.constants.UserProfileErrorCodeConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class UserCreationRequest {

    @NotNull(message = UserErrorCodeConstants.USER_USERNAME_INVALID)
    @NotBlank(message = UserErrorCodeConstants.USER_USERNAME_INVALID)
    @Size(min = UserConstants.MIN_CHARS_USERNAME,
            max = UserConstants.MAX_CHARS_USERNAME,
            message = UserErrorCodeConstants.USER_USERNAME_INVALID)
    String username;

    @NotNull(message = UserErrorCodeConstants.USER_PASSWORD_INVALID)
    @NotBlank(message = UserErrorCodeConstants.USER_PASSWORD_INVALID)
    @Size(min = UserConstants.MIN_CHARS_PASSWORD,
            max = UserConstants.MAX_CHARS_PASSWORD,
            message = UserErrorCodeConstants.USER_PASSWORD_INVALID)
    String password;

    @NotNull(message = UserErrorCodeConstants.USER_EMAIL_INVALID)
    @NotBlank(message = UserErrorCodeConstants.USER_EMAIL_INVALID)
    @Email(message = UserErrorCodeConstants.USER_EMAIL_INVALID)
    String email;

    @NotNull(message = UserProfileErrorCodeConstants.USER_FULL_NAME_INVALID)
    @NotBlank(message = UserProfileErrorCodeConstants.USER_FULL_NAME_INVALID)
    @Size(min = UserProfileConstants.MIN_CHARS_FULL_NAME,
            max = UserProfileConstants.MAX_CHARS_FULL_NAME,
            message = UserProfileErrorCodeConstants.USER_FULL_NAME_INVALID)
    String fullName;

    @NotNull(message = UserProfileErrorCodeConstants.USER_PHONE_INVALID)
    @NotBlank(message = UserProfileErrorCodeConstants.USER_PHONE_INVALID)
    @Pattern(regexp = UserProfileConstants.PHONE_PATTERN,
            message = UserProfileErrorCodeConstants.USER_PHONE_INVALID)
    String phone;
}
