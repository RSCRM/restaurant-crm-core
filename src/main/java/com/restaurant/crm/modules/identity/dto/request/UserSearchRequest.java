package com.restaurant.crm.modules.identity.dto.request;

import com.restaurant.crm.modules.identity.enums.UserStatus;
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
public class UserSearchRequest {
    // Search
    String username;
    String email;

    // Filter
    UserStatus status;
    String roleName;
}
