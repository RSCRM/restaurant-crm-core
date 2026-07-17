package com.restaurant.crm.modules.identity.dto.request;

import com.fsoft.erp.modules.identity.constants.role.RoleConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleCreationRequest {
    @NotBlank
    @Size(min = RoleConstants.MIN_CHARS_ROLE_NAME,
            max = RoleConstants.MAX_CHARS_ROLE_NAME)
    String roleName;

    List<String> permissionIds;
}
