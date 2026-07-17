package com.restaurant.crm.modules.identity.mapper;

import com.restaurant.crm.modules.identity.dto.request.RoleCreationRequest;
import com.restaurant.crm.modules.identity.dto.response.RoleResponse;
import com.restaurant.crm.modules.identity.entity.Permission;
import com.restaurant.crm.modules.identity.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    Role toRole(RoleCreationRequest req);
    
    @Mapping(target = "permissions", source = "role.permissions")
    RoleResponse toRoleResponse(Role role);
    
    default Set<String> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        return permissions.stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());
    }
}
