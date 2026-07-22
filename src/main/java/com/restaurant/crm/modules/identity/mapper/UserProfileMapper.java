package com.restaurant.crm.modules.identity.mapper;

import com.restaurant.crm.modules.identity.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "fullName", source = "profile.fullName")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "profile.phone")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "createdAt", source = "user.createdAt")
    UserProfileResponse toUserProfileResponse(User user, UserProfile profile);
}
