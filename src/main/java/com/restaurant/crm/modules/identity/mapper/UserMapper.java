package com.restaurant.crm.modules.identity.mapper;

import com.restaurant.crm.modules.identity.dto.request.UserCreationRequest;
import com.restaurant.crm.modules.identity.dto.response.UserResponse;
import com.restaurant.crm.modules.identity.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}
