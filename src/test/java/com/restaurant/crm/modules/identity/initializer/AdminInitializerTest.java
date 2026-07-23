package com.restaurant.crm.modules.identity.initializer;

import com.restaurant.crm.common.properties.AdminProperties;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.profile.constants.UserProfileConstants;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserProfileRepository userProfileRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AdminProperties adminProperties;
    @Mock
    ApplicationArguments applicationArguments;
    @InjectMocks
    AdminInitializer adminInitializer;

    @Test
    void createsMissingProfileForExistingAdmin() {
        User admin = User.builder().id("admin-1").username("admin").build();
        when(adminProperties.getUsername()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userProfileRepository.findByUser_Id("admin-1")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        adminInitializer.run(applicationArguments);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertSame(admin, captor.getValue().getUser());
        assertEquals(UserProfileConstants.DEFAULT_ADMIN_FULL_NAME, captor.getValue().getFullName());
        assertNull(captor.getValue().getPhone());
    }
}
