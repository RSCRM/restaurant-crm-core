package com.restaurant.crm.modules.crm.pointwallet.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.crm.pointwallet.constants.permission.PointWalletPermissionConstants;
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
public class PointWalletPermissionInitializer implements ApplicationRunner {

    OrgPermissionRepository orgPermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Initializing Point Wallet permissions...");
        Set<String> names = Set.of(
                PointWalletPermissionConstants.POINT_WALLET_READ,
                PointWalletPermissionConstants.POINT_WALLET_ADJUST
        );
        names.forEach(name -> {
            if (!orgPermissionRepository.existsByPermissionName(name)) {
                orgPermissionRepository.save(OrgPermission.builder().permissionName(name).build());
            }
        });
    }
}
