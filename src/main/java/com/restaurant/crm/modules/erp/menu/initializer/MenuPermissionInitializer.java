package com.restaurant.crm.modules.erp.menu.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.erp.menu.constants.permission.MenuPermissionConstants;
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
public class MenuPermissionInitializer implements ApplicationRunner {

    OrgPermissionRepository orgPermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> names = Set.of(
                MenuPermissionConstants.CATEGORY_ADD, MenuPermissionConstants.CATEGORY_UPDATE, MenuPermissionConstants.CATEGORY_DELETE,
                MenuPermissionConstants.PRODUCT_ADD, MenuPermissionConstants.PRODUCT_UPDATE, MenuPermissionConstants.PRODUCT_DELETE,
                MenuPermissionConstants.COMBO_ADD, MenuPermissionConstants.COMBO_UPDATE, MenuPermissionConstants.COMBO_DELETE
        );
        names.forEach(name -> {
            if (!orgPermissionRepository.existsByPermissionName(name)) {
                orgPermissionRepository.save(OrgPermission.builder().permissionName(name).build());
            }
        });
    }
}
