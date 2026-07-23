package com.restaurant.crm.modules.identity.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.identity.constants.permission.StartDefinedPermission;
import com.restaurant.crm.modules.identity.entity.Permission;
import com.restaurant.crm.modules.identity.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Order(InitializerOrder.PERMISSION)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionInitializer implements ApplicationRunner {
    PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<Permission> permissions = getPermissions();
        permissions.forEach(permission -> {
            if (!permissionRepository.existsByPermissionName(permission.getPermissionName())) {
                permissionRepository.save(permission);
            }
        });
    }

    private Set<Permission> getPermissions() {
        return Set.of(
                // ===== SYSTEM =====
                Permission.builder()
                        .permissionName(StartDefinedPermission.SYSTEM_VIEW)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.SYSTEM_MANAGE)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.ROLE_VIEW)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.ROLE_MANAGE)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.PERMISSION_VIEW)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.PERMISSION_MANAGE)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.AUDIT_VIEW)
                        .build(),

                // ===== USER =====
                Permission.builder()
                        .permissionName(StartDefinedPermission.USER_VIEW)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.USER_UPDATE)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.USER_DELETE)
                        .build(),

                // ===== BRANCH MANAGER =====
                Permission.builder()
                        .permissionName(StartDefinedPermission.BRANCH_MANAGER_VIEW)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.BRANCH_MANAGER_CREATE)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.BRANCH_MANAGER_UPDATE)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.BRANCH_MANAGER_DELETE)
                        .build(),
                Permission.builder()
                        .permissionName(StartDefinedPermission.BRANCH_MANAGER_ASSIGN)
                        .build(),

                // ===== CONTRACT =====
                Permission.builder()
                        .permissionName(StartDefinedPermission.CONTRACT_LICENSE_VIEW)
                        .build()
        );
    }
}
