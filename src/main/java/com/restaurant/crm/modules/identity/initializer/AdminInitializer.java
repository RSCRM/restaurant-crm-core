package com.restaurant.crm.modules.identity.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.properties.AdminProperties;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
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

import java.util.Set;

@Order(InitializerOrder.ADMIN)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminInitializer implements ApplicationRunner {
    UserRepository usersRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
         log.info("Initializing admin ...");

        boolean existed = usersRepository.existsByUsername(adminProperties.getUsername());
        if (!existed) {
            Role adminRole = roleRepository.findByRoleName(PredefinedRole.ADMIN_ROLE)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            User admin = User.builder()
                    .username(adminProperties.getUsername())
                    .password(passwordEncoder.encode(adminProperties.getPassword()))
                    .email(adminProperties.getUsername())
                    .enabled(true)
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(adminRole))
                    .build();
            usersRepository.save(admin);

            log.info("Admin has been created with username {}", admin.getUsername());
        } else {
            log.info("Admin already exists with username {}", adminProperties.getUsername());
        }
    }
}

