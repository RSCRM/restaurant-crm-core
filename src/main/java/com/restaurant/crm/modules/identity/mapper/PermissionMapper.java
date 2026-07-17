package com.restaurant.crm.modules.identity.mapper;

import com.restaurant.crm.modules.identity.dto.response.PermissionResponse;
import com.restaurant.crm.modules.identity.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toPermissionResponse(Permission permission);
}
