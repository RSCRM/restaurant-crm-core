package com.restaurant.crm.modules.erp.organization.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.repository.OrgPermissionRepository;
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
public class OrgPermissionInitializer implements ApplicationRunner {

    OrgPermissionRepository orgPermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> names = Set.of(
                EmployeeConstants.EMPLOYEE_ADD,
                EmployeeConstants.EMPLOYEE_UPDATE,
                EmployeeConstants.EMPLOYEE_DELETE,
                EmployeeConstants.EMPLOYEE_ROLE_ASSIGN,
                EmployeeConstants.EMPLOYEE_ROLE_REVOKE,
                // Kitchen Display (KDS) permissions — uc-scf-01..06
                StartDefinedOrgPermission.KITCHEN_ORDER_READ,
                StartDefinedOrgPermission.KITCHEN_ITEM_ACCEPT,
                StartDefinedOrgPermission.KITCHEN_ITEM_UPDATE,
                StartDefinedOrgPermission.KITCHEN_ITEM_CANCEL
        );
        names.forEach(name -> {
            if (!orgPermissionRepository.existsByPermissionName(name)) {
                orgPermissionRepository.save(OrgPermission.builder().permissionName(name).build());
            }
        });
    }
}
