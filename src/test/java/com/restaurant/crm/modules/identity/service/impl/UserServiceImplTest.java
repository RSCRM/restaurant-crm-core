package com.restaurant.crm.modules.identity.service.impl;

import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.dto.request.UserCreationRequest;
import com.restaurant.crm.modules.identity.dto.response.UserResponse;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.mapper.UserMapper;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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

}
