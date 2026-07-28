package com.restaurant.crm.modules.identity.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.identity.dto.request.UserCreationRequest;
import com.restaurant.crm.modules.identity.dto.request.UserRolesUpdateRequest;
import com.restaurant.crm.modules.identity.dto.response.UserResponse;

public interface UserService {

    UserResponse create(UserCreationRequest request);

    PagingResponse<UserResponse> getUsers(PagingRequest request);

    UserResponse getById(String userId);

    UserResponse updateRoles(String userId, UserRolesUpdateRequest request);

    void softDeleteById(String userId);

}
