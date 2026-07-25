package com.restaurant.crm.modules.erp.employee.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.erp.employee.constants.permission.OrgPermissionConstants;
import com.restaurant.crm.modules.erp.employee.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
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
                OrgPermissionConstants.EMPLOYEE_ADD,
                OrgPermissionConstants.EMPLOYEE_UPDATE,
                OrgPermissionConstants.EMPLOYEE_DELETE,
                OrgPermissionConstants.EMPLOYEE_ROLE_ASSIGN,
                OrgPermissionConstants.EMPLOYEE_ROLE_REVOKE
        );
        names.forEach(name -> {
            if (!orgPermissionRepository.existsByPermissionName(name)) {
                orgPermissionRepository.save(OrgPermission.builder().permissionName(name).build());
            }
        });
    }
}
