package com.restaurant.crm.modules.erp.organization.dto.request;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrgRoleRequest {
    @NotBlank(message = "ORG_ROLE_NAME_REQUIRED")
    @Size(min = 1, max = 50)
    String roleName;
    List<String> permissionIds;
}
