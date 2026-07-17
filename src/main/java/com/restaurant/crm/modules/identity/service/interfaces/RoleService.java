package com.restaurant.crm.modules.identity.service.interfaces;


import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.identity.dto.request.RoleUpdateRequest;
import com.restaurant.crm.modules.identity.dto.response.RoleResponse;

public interface RoleService {

    PagingResponse<RoleResponse> getRoles(PagingRequest request);

    RoleResponse getById(String roleId);

    RoleResponse update(String roleId, RoleUpdateRequest request);

    void deleteById(String roleId);
}

