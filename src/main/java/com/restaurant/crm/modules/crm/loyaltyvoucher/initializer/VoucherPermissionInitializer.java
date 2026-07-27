package com.restaurant.crm.modules.crm.loyaltyvoucher.initializer;

import com.restaurant.crm.common.constant.InitializerOrder;
import com.restaurant.crm.modules.crm.loyaltyvoucher.constants.permission.VoucherPermissionConstants;
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
public class VoucherPermissionInitializer implements ApplicationRunner {

    OrgPermissionRepository orgPermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Initializing Voucher permissions...");
        Set<String> names = Set.of(
                VoucherPermissionConstants.VOUCHER_CREATE,
                VoucherPermissionConstants.VOUCHER_READ,
                VoucherPermissionConstants.VOUCHER_UPDATE,
                VoucherPermissionConstants.CUSTOMER_VOUCHER_READ,
                VoucherPermissionConstants.CUSTOMER_VOUCHER_REDEEM,
                VoucherPermissionConstants.CUSTOMER_VOUCHER_GIVE,
                VoucherPermissionConstants.CUSTOMER_VOUCHER_USE
        );
        names.forEach(name -> {
            if (!orgPermissionRepository.existsByPermissionName(name)) {
                orgPermissionRepository.save(OrgPermission.builder().permissionName(name).build());
            }
        });
    }
}
