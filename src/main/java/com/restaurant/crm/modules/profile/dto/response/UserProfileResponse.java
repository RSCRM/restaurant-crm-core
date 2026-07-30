package com.restaurant.crm.modules.profile.dto.response;

import com.restaurant.crm.modules.identity.dto.response.RoleResponse;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    String id;
    String userId;
    String fullName;
    String username;
    String email;
    String phone;
    UserStatus status;
    Set<RoleResponse> roles;
    Instant createdAt;
}
