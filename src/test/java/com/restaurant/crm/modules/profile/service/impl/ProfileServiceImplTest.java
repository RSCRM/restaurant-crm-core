package com.restaurant.crm.modules.profile.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.profile.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.mapper.UserProfileMapper;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import com.restaurant.crm.modules.profile.dto.request.ProfileUpdateRequest;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserProfileRepository userProfileRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Spy
    UserProfileMapper userProfileMapper = Mappers.getMapper(UserProfileMapper.class);
    @InjectMocks
    ProfileServiceImpl profileService;

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

            UserProfileResponse response = profileService.getMyInfo();

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

            UserProfileResponse response = profileService.getMyInfo();

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

            AppException exception = assertThrows(AppException.class, profileService::getMyInfo);
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void updateMyInfoRequiresEmployeeAccessToBeEnabled() {
        User user = User.builder().id("user-1").build();
        Employee employee = Employee.builder()
                .id("employee-1")
                .user(user)
                .profileUpdateEnabled(false)
                .build();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("New Name");

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            authUtils.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> profileService.updateMyInfo(request));

            assertEquals(ErrorCode.AUTHZ_UNAUTHORIZED, exception.getErrorCode());
        }
    }
}
