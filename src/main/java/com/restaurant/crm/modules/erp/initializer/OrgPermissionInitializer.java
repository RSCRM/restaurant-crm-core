package com.restaurant.crm.modules.erp.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.erp.constants.org_permission.PredefinedOrgPermission;
import com.restaurant.crm.modules.erp.entity.OrgPermission;
import com.restaurant.crm.modules.erp.repository.OrgPermissionRepository;
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

@Order(InitializerOrder.PERMISSION + 1)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrgPermissionInitializer implements ApplicationRunner {
    OrgPermissionRepository orgPermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        getPermissions().forEach(permission -> {
            if (!orgPermissionRepository.existsByPermissionCode(permission.getPermissionCode())) {
                orgPermissionRepository.save(permission);
            }
        });
    }

    private Set<OrgPermission> getPermissions() {
        return Set.of(
                OrgPermission.builder()
                        .permissionCode(PredefinedOrgPermission.MANAGE_TABLE)
                        .permissionName("Manage table")
                        .build(),
                OrgPermission.builder()
                        .permissionCode(PredefinedOrgPermission.MANAGE_MENU)
                        .permissionName("Manage menu")
                        .build(),
                OrgPermission.builder()
                        .permissionCode(PredefinedOrgPermission.MANAGE_EMPLOYEE)
                        .permissionName("Manage employee")
                        .build(),
                OrgPermission.builder()
                        .permissionCode(PredefinedOrgPermission.MANAGE_INVENTORY)
                        .permissionName("Manage inventory")
                        .build(),
                OrgPermission.builder()
                        .permissionCode(PredefinedOrgPermission.VIEW_BRANCH_REPORT)
                        .permissionName("View branch report")
                        .build()
        );
    }
}
