package com.restaurant.crm.modules.erp.organization.dto.response;

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
public class OrgRoleResponse {
    String id;
    String organizationId;
    String roleName;
    String dataScope;
    List<OrgPermissionResponse> permissions;
}
