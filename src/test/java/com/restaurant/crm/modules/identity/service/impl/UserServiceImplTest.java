package com.restaurant.crm.modules.identity.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.dto.request.UserCreationRequest;
import com.restaurant.crm.modules.identity.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.identity.dto.response.UserResponse;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.entity.UserProfile;
import com.restaurant.crm.modules.identity.mapper.UserMapper;
import com.restaurant.crm.modules.identity.mapper.UserProfileMapper;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserProfileRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    UserMapper userMapper;
    @Mock
    UserProfileRepository userProfileRepository;
    @Spy
    UserProfileMapper userProfileMapper = Mappers.getMapper(UserProfileMapper.class);
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    UserServiceImpl userService;

    @Test
    void createUserCreatesProfileInSameFlow() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("owner")
                .password("Password@1")
                .email("owner@example.com")
                .fullName("Restaurant Owner")
                .phone("0900000001")
                .build();
        User user = User.builder().password(request.getPassword()).build();
        User savedUser = User.builder().id("owner-1").build();
        Role userRole = Role.builder().roleName(PredefinedRole.USER_ROLE).build();
        UserResponse expected = UserResponse.builder().id("owner-1").build();

        when(userMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(roleRepository.findByRoleName(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(userRole));
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(savedUser)).thenReturn(expected);

        assertSame(expected, userService.create(request));

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertSame(savedUser, captor.getValue().getUser());
        assertEquals("Restaurant Owner", captor.getValue().getFullName());
        assertEquals("0900000001", captor.getValue().getPhone());
    }

    @Test
    void getMyInfoReturnsAuthenticatedUserProfile() {
        Instant createdAt = Instant.parse("2026-07-22T00:00:00Z");
        Role adminRole = Role.builder()
                .id("role-1")
                .roleName(PredefinedRole.ADMIN_ROLE)
                .permissions(Set.of())
                .build();
        User user = User.builder()
                .id("user-1")
                .username("admin")
                .email("admin@example.com")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .createdAt(createdAt)
                .build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName("System Admin")
                .phone("0900000000")
                .build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(userProfileRepository.findByUser_Id("user-1")).thenReturn(Optional.of(profile));

            UserProfileResponse response = userService.getMyInfo();

            assertEquals("user-1", response.getId());
            assertEquals("System Admin", response.getFullName());
            assertEquals("admin", response.getUsername());
            assertEquals("admin@example.com", response.getEmail());
            assertEquals("0900000000", response.getPhone());
            assertEquals(UserStatus.ACTIVE, response.getStatus());
            assertEquals(Set.of(PredefinedRole.ADMIN_ROLE), response.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(java.util.stream.Collectors.toSet()));
            assertEquals(createdAt, response.getCreatedAt());
            verify(userRepository).findById("user-1");
        }
    }

    @Test
    void getMyInfoStillReturnsLoginDataWithoutProfile() {
        User user = User.builder()
                .id("user-1")
                .username("admin")
                .email("admin@example.com")
                .status(UserStatus.ACTIVE)
                .roles(Set.of())
                .build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(userProfileRepository.findByUser_Id("user-1")).thenReturn(Optional.empty());

            UserProfileResponse response = userService.getMyInfo();

            assertEquals("user-1", response.getId());
            assertEquals("admin", response.getUsername());
            assertEquals("admin@example.com", response.getEmail());
            assertNull(response.getFullName());
            assertNull(response.getPhone());
        }
    }

    @Test
    void getMyInfoRejectsMissingUser() {
        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUserId).thenReturn("missing");
            when(userRepository.findById("missing")).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, userService::getMyInfo);
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }
    }
}
