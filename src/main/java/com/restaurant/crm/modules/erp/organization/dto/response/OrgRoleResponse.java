package com.restaurant.crm.modules.erp.organization.dto.response;

import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrgRoleResponse {
    String id;
    String roleName;
    OrgDataScope dataScope;
}
