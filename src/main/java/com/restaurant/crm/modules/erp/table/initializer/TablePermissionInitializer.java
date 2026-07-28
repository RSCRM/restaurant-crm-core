package com.restaurant.crm.modules.erp.table.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.table.constants.permission.TablePermissionConstants;
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

@Order(InitializerOrder.ORG_PERMISSION)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TablePermissionInitializer implements ApplicationRunner {

    OrgPermissionRepository orgPermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!orgPermissionRepository.existsByPermissionName(StartDefinedOrgPermission.TABLE_SESSION_CREATE)) {
            orgPermissionRepository.save(OrgPermission.builder()
                    .permissionName(StartDefinedOrgPermission.TABLE_SESSION_CREATE)
                    .build());
        }
        Set<String> names = Set.of(
                StartDefinedOrgPermission.TABLE_MAP_READ,
                TablePermissionConstants.TABLE_AREA_ADD,
                TablePermissionConstants.TABLE_AREA_UPDATE,
                TablePermissionConstants.TABLE_AREA_DELETE,
                TablePermissionConstants.RESTAURANT_TABLE_ADD,
                TablePermissionConstants.RESTAURANT_TABLE_UPDATE,
                TablePermissionConstants.RESTAURANT_TABLE_DELETE
        );
        names.forEach(name -> {
            if (!orgPermissionRepository.existsByPermissionName(name)) {
                orgPermissionRepository.save(OrgPermission.builder().permissionName(name).build());
            }
        });
    }
}
