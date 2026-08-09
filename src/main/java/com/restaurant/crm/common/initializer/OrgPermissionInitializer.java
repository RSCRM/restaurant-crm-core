//package com.restaurant.crm.common.initializer;
//
//import com.restaurant.crm.common.constant.InitializerOrder;
//import com.restaurant.crm.modules.crm.customeraccount.constants.permission.CustomerPermissionConstants;
//import com.restaurant.crm.modules.crm.loyaltyvoucher.constants.permission.VoucherPermissionConstants;
//import com.restaurant.crm.modules.crm.pointwallet.constants.permission.PointWalletPermissionConstants;
//import com.restaurant.crm.modules.erp.attendance.constants.permission.AttendancePermissionConstants;
//import com.restaurant.crm.modules.erp.booking.constants.permission.BookingPermissionConstants;
//import com.restaurant.crm.modules.erp.menu.constants.permission.MenuPermissionConstants;
//import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
//import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
//import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
//import com.restaurant.crm.modules.erp.organization.repository.OrgPermissionRepository;
//import com.restaurant.crm.modules.erp.schedule.constants.SchedulePermissionConstants;
//import com.restaurant.crm.modules.erp.table.constants.permission.TablePermissionConstants;
//import com.restaurant.crm.modules.profile.constants.permission.ProfilePermissionConstants;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Set;
//
//@Order(InitializerOrder.ORG_PERMISSION)
//@Component
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Slf4j
//public class OrgPermissionInitializer implements ApplicationRunner {
//
//    OrgPermissionRepository orgPermissionRepository;
//
//    @Override
//    @Transactional
//    public void run(ApplicationArguments args) {
//        log.info("Initializing OrgPermissions...");
//
//        Set<String> allPermissions = Set.of(
//                // ── Order ──
//                StartDefinedOrgPermission.ORDER_READ,
//                StartDefinedOrgPermission.ORDER_CREATE,
//                StartDefinedOrgPermission.ORDER_UPDATE,
//                StartDefinedOrgPermission.ORDER_DELETE,
//
//                // ── Payment ──
//                StartDefinedOrgPermission.PAYMENT_READ,
//                StartDefinedOrgPermission.PAYMENT_CREATE,
//
//                // ── Operations & Setup ──
//                StartDefinedOrgPermission.MENU_MANAGE,
//                StartDefinedOrgPermission.TABLE_MANAGE,
//                StartDefinedOrgPermission.TABLE_MAP_READ,
//                StartDefinedOrgPermission.TABLE_SEARCH_READ,
//                StartDefinedOrgPermission.TABLE_SESSION_CREATE,
//                StartDefinedOrgPermission.REPORT_VIEW,
//
//                // ── Management & Administration ──
//                StartDefinedOrgPermission.STAFF_MANAGE,
//                StartDefinedOrgPermission.BRANCH_MANAGE,
//                StartDefinedOrgPermission.ORG_MANAGE,
//                StartDefinedOrgPermission.ORG_ROLE_MANAGE,
//
//                // ===== ORGANIZATION =====
//                StartDefinedOrgPermission.ORGANIZATION_VIEW,
//                StartDefinedOrgPermission.ORGANIZATION_MANAGE,
//
//                // ===== ORGANIZATION BRANCH =====
//                StartDefinedOrgPermission.ORGANIZATION_BRANCH_VIEW,
//                StartDefinedOrgPermission.ORGANIZATION_BRANCH_MANAGE,
//
//                // ===== INVENTORY CATEGORY =====
//                StartDefinedOrgPermission.INVENTORY_CATEGORY_VIEW,
//                StartDefinedOrgPermission.INVENTORY_CATEGORY_MANAGE,
//
//                // ===== INVENTORY =====
//                StartDefinedOrgPermission.INVENTORY_VIEW,
//                StartDefinedOrgPermission.INVENTORY_MANAGE,
//
//                // ===== INVENTORY TRANSACTION =====
//                StartDefinedOrgPermission.INVENTORY_TRANSACTION_VIEW,
//                StartDefinedOrgPermission.INVENTORY_TRANSACTION_MANAGE,
//
//                // ── Employee ──
//                EmployeeConstants.EMPLOYEE_ADD,
//                EmployeeConstants.EMPLOYEE_VIEW,
//                EmployeeConstants.EMPLOYEE_UPDATE,
//                EmployeeConstants.EMPLOYEE_DELETE,
//                EmployeeConstants.EMPLOYEE_ROLE_ASSIGN,
//                EmployeeConstants.EMPLOYEE_ROLE_REVOKE,
//
//                // ── Schedule ──
//                SchedulePermissionConstants.SCHEDULE_STAFF_READ,
//                SchedulePermissionConstants.SCHEDULE_MANAGE,
//
//                // ── Profile ──
//                ProfilePermissionConstants.SELF_UPDATE,
//
//                // ── Table ──
//                TablePermissionConstants.TABLE_AREA_ADD,
//                TablePermissionConstants.TABLE_AREA_UPDATE,
//                TablePermissionConstants.TABLE_AREA_DELETE,
//                TablePermissionConstants.RESTAURANT_TABLE_ADD,
//                TablePermissionConstants.RESTAURANT_TABLE_UPDATE,
//                TablePermissionConstants.RESTAURANT_TABLE_DELETE,
//
//                // ── Menu ──
//                MenuPermissionConstants.CATEGORY_ADD,
//                MenuPermissionConstants.CATEGORY_UPDATE,
//                MenuPermissionConstants.CATEGORY_DELETE,
//                MenuPermissionConstants.PRODUCT_ADD,
//                MenuPermissionConstants.PRODUCT_UPDATE,
//                MenuPermissionConstants.PRODUCT_DELETE,
//                MenuPermissionConstants.COMBO_ADD,
//                MenuPermissionConstants.COMBO_UPDATE,
//                MenuPermissionConstants.COMBO_DELETE,
//
//                // ── Booking ──
//                BookingPermissionConstants.BOOKING_CREATE,
//                BookingPermissionConstants.BOOKING_READ,
//                BookingPermissionConstants.BOOKING_UPDATE,
//
//                // ── Attendance ──
//                AttendancePermissionConstants.QR_DISPLAY,
//                AttendancePermissionConstants.SELF_WRITE,
//                AttendancePermissionConstants.SELF_READ,
//                AttendancePermissionConstants.BRANCH_READ,
//
//                // ── Voucher ──
//                VoucherPermissionConstants.VOUCHER_CREATE,
//                VoucherPermissionConstants.VOUCHER_READ,
//                VoucherPermissionConstants.VOUCHER_UPDATE,
//                VoucherPermissionConstants.CUSTOMER_VOUCHER_READ,
//                VoucherPermissionConstants.CUSTOMER_VOUCHER_REDEEM,
//                VoucherPermissionConstants.CUSTOMER_VOUCHER_GIVE,
//                VoucherPermissionConstants.CUSTOMER_VOUCHER_USE,
//
//                // ── Customer ──
//                CustomerPermissionConstants.CUSTOMER_CREATE,
//                CustomerPermissionConstants.CUSTOMER_READ,
//
//                // ── Point Wallet ──
//                PointWalletPermissionConstants.POINT_WALLET_READ,
//                PointWalletPermissionConstants.POINT_WALLET_ADJUST
//        );
//
//        allPermissions.forEach(name -> {
//            if (!orgPermissionRepository.existsByPermissionName(name)) {
//                orgPermissionRepository.save(OrgPermission.builder().permissionName(name).build());
//            }
//        });
//
//        log.info("OrgPermissions initialized: {} permissions ensured", allPermissions.size());
//    }
//}
