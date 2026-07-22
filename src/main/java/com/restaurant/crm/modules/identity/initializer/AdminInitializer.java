package com.restaurant.crm.modules.identity.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.properties.AdminProperties;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.entity.UserProfile;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserProfileRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Order(InitializerOrder.ADMIN)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminInitializer implements ApplicationRunner {
    UserRepository usersRepository;
    UserProfileRepository userProfileRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Initializing admin ...");

        User admin = usersRepository.findByUsername(adminProperties.getUsername()).orElseGet(() -> {
            Role adminRole = roleRepository.findByRoleName(PredefinedRole.ADMIN_ROLE)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            User createdAdmin = User.builder()
                    .username(adminProperties.getUsername())
                    .password(passwordEncoder.encode(adminProperties.getPassword()))
                    .email(adminProperties.getUsername() + "@admin.local")
                    .enabled(true)
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(adminRole))
                    .build();
            User savedAdmin = usersRepository.save(createdAdmin);

            log.info("Admin has been created with username {}", savedAdmin.getUsername());
            return savedAdmin;
        });

        userProfileRepository.findByUser_Id(admin.getId()).orElseGet(() ->
                userProfileRepository.save(UserProfile.builder()
                        .user(admin)
                        .fullName(adminProperties.getFullName())
                        .phone(StringUtils.hasText(adminProperties.getPhone())
                                ? adminProperties.getPhone()
                                : null)
                        .build()));
    }
}
