package com.restaurant.crm.modules.crm.loyaltyvoucher.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherApplicableResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.Voucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.CustomerVoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.VoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces.CustomerLoyaltyService;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;
import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPoint;
import com.restaurant.crm.modules.crm.pointwallet.mapper.CustomerPointMapper;
import com.restaurant.crm.modules.crm.pointwallet.repository.CustomerPointRepository;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Customer-facing loyalty service implementation.
 * Resolves customerId, branchId, orderId from the CUSTOMER_SESSION token in Redis.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerLoyaltyServiceImpl implements CustomerLoyaltyService {

    QrSessionRedisRepository qrSessionRedisRepository;
    OrderRepository orderRepository;
    OrganizationBranchRepository branchRepository;
    CustomerRepository customerRepository;
    CustomerPointRepository customerPointRepository;
    CustomerPointMapper customerPointMapper;
    VoucherRepository voucherRepository;
    CustomerVoucherRepository customerVoucherRepository;

    // ======== Public API ========

    @Override
    public CustomerPointResponse getMyPoints() {
        QrSessionData session = currentSession();
        String organizationId = resolveOrganizationId(session.branchId());
        Customer customer = findCustomerByPhone(session.ownerCustomerPhone());

        Optional<CustomerPoint> walletOpt = customerPointRepository.findByCustomerIdAndOrganizationId(customer.getId(), organizationId);
        if (walletOpt.isEmpty()) {
            return CustomerPointResponse.builder()
                    .customerId(customer.getId())
                    .organizationId(organizationId)
                    .currentPoints(0)
                    .lifetimePoints(0)
                    .build();
        }
        return customerPointMapper.toCustomerPointResponse(walletOpt.get());
    }

    @Override
    public List<CustomerVoucherApplicableResponse> getVoucherCatalog() {
        QrSessionData session = currentSession();
        String branchId = session.branchId();
        Customer customer = findCustomerByPhone(session.ownerCustomerPhone());
        String organizationId = resolveOrganizationId(branchId);

        // Load current points
        int currentPoints = customerPointRepository.findByCustomerIdAndOrganizationId(customer.getId(), organizationId)
                .map(CustomerPoint::getCurrentPoints)
                .orElse(0);

        // Load active and unexpired vouchers for this branch (catalog should NEVER show expired vouchers)
        List<Voucher> vouchers = voucherRepository.findByBranchIdAndIsActive(branchId, (short) 1).stream()
                .filter(v -> (v.getStartAt() == null || !Instant.now().isBefore(v.getStartAt()))
                        && (v.getEndAt() == null || Instant.now().isBefore(v.getEndAt()))
                        && (v.getExpiredAt() == null || Instant.now().isBefore(v.getExpiredAt())))
                .toList();

        // Load vouchers already owned by this customer
        List<CustomerVoucher> ownedVouchers = customerVoucherRepository.findByCustomerIdAndBranchId(customer.getId(), branchId);
        java.util.Set<String> ownedVoucherIds = ownedVouchers.stream()
                .map(cv -> cv.getVoucher().getId())
                .collect(java.util.stream.Collectors.toSet());

        return vouchers.stream().map(v -> {
            boolean canAfford = currentPoints >= v.getPointsRequired();
            boolean alreadyOwned = customerVoucherRepository.existsByCustomerIdAndVoucherId(customer.getId(), v.getId());

            boolean isRedeemable = true;
            String reason = null;

            if (alreadyOwned) {
                isRedeemable = false;
                reason = "Bạn đã nhận/đổi Voucher này rồi (đã có trong Ví)";
            } else if (!canAfford) {
                isRedeemable = false;
                reason = "Bạn cần " + v.getPointsRequired() + " điểm để đổi (hiện có " + currentPoints + " điểm)";
            }

            return CustomerVoucherApplicableResponse.builder()
                    .customerVoucherId(v.getId()) // This is voucherId in catalog context
                    .voucherSn(null)
                    .title(v.getTitle())
                    .discountPercent(v.getDiscountPercent())
                    .minBillAmount(v.getMinBillAmount())
                    .pointsRequired(v.getPointsRequired())
                    .status(alreadyOwned ? "OWNED" : "ACTIVE")
                    .expiredAt(v.getExpiredAt())
                    .isApplicable(isRedeemable)
                    .reason(reason)
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public String redeemVoucher(String voucherId) {
        QrSessionData session = currentSession();
        String branchId = session.branchId();
        Customer customer = findCustomerByPhone(session.ownerCustomerPhone());
        String organizationId = resolveOrganizationId(branchId);

        // Check if customer already owns this voucher
        if (customerVoucherRepository.existsByCustomerIdAndVoucherId(customer.getId(), voucherId)) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_ALREADY_USED);
        }

        // 1. Load voucher
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (voucher.getIsActive() != 1) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }

        // Check expiry
        if (voucher.getStartAt() != null && Instant.now().isBefore(voucher.getStartAt())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_STARTED_YET);
        }
        if (voucher.getEndAt() != null && Instant.now().isAfter(voucher.getEndAt())) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED);
        }
        if (voucher.getExpiredAt() != null && Instant.now().isAfter(voucher.getExpiredAt())) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED);
        }

        // 2. Check & deduct points (if points > 0)
        if (voucher.getPointsRequired() > 0) {
            CustomerPoint wallet = customerPointRepository.findByCustomerIdAndOrganizationId(customer.getId(), organizationId)
                    .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_POINT_NOT_FOUND));

            if (wallet.getCurrentPoints() < voucher.getPointsRequired()) {
                throw new AppException(ErrorCode.CUSTOMER_POINT_INSUFFICIENT);
            }

            wallet.setCurrentPoints(wallet.getCurrentPoints() - voucher.getPointsRequired());
            customerPointRepository.save(wallet);
        }

        // 3. Create customer voucher
        String voucherSn = generateUniqueVoucherSn();
        CustomerVoucher customerVoucher = CustomerVoucher.builder()
                .customer(customer)
                .branchId(branchId)
                .voucher(voucher)
                .voucherSn(voucherSn)
                .status(CustomerVoucherStatus.AVAILABLE)
                .build();
        customerVoucher = customerVoucherRepository.save(customerVoucher);

        return customerVoucher.getId();
    }

    @Override
    public List<CustomerVoucherApplicableResponse> getMyApplicableVouchers() {
        QrSessionData session = currentSession();
        String branchId = session.branchId();
        Customer customer = findCustomerByPhone(session.ownerCustomerPhone());

        // Get current order subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        if (session.orderId() != null && !session.orderId().isBlank()) {
            Order order = orderRepository.findById(session.orderId()).orElse(null);
            if (order != null) {
                subtotal = order.getSubtotal();
            }
        }

        // Load all vouchers owned by this customer at this branch
        List<CustomerVoucher> customerVouchers = customerVoucherRepository.findByCustomerIdAndBranchId(customer.getId(), branchId);

        BigDecimal finalSubtotal = subtotal;
        return customerVouchers.stream().map(cv -> {
            Voucher voucher = cv.getVoucher();
            boolean notStarted = voucher.getStartAt() != null && Instant.now().isBefore(voucher.getStartAt());
            boolean isExpired = (voucher.getEndAt() != null && Instant.now().isAfter(voucher.getEndAt()))
                    || (voucher.getExpiredAt() != null && Instant.now().isAfter(voucher.getExpiredAt()));

            boolean isApplicable = true;
            String reason = null;
            String status = cv.getStatus().name();

            if (notStarted) {
                isApplicable = false;
                reason = "Voucher chưa đến ngày áp dụng";
                status = "NOT_STARTED";
            } else if (isExpired) {
                isApplicable = false;
                reason = "Voucher đã hết hạn sử dụng";
                status = "EXPIRED";
            } else if (cv.getStatus() != CustomerVoucherStatus.AVAILABLE) {
                isApplicable = false;
                reason = "Voucher đã được sử dụng";
            } else if (finalSubtotal.compareTo(voucher.getMinBillAmount()) < 0) {
                isApplicable = false;
                reason = "Đơn hàng tối thiểu " + voucher.getMinBillAmount().toPlainString() + "đ (hiện " + finalSubtotal.toPlainString() + "đ)";
            }

            return CustomerVoucherApplicableResponse.builder()
                    .customerVoucherId(cv.getId())
                    .voucherSn(cv.getVoucherSn())
                    .title(voucher.getTitle())
                    .discountPercent(voucher.getDiscountPercent())
                    .minBillAmount(voucher.getMinBillAmount())
                    .pointsRequired(voucher.getPointsRequired())
                    .status(status)
                    .expiredAt(voucher.getExpiredAt())
                    .isApplicable(isApplicable)
                    .reason(reason)
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public void applyVoucherToCurrentOrder(String customerVoucherId) {
        QrSessionData session = currentSession();
        String orderId = session.orderId();
        if (orderId == null || orderId.isBlank()) {
            throw new AppException(ErrorCode.TRACK_NO_ACTIVE_ORDER);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // 1. Release any existing voucher on this order first
        customerVoucherRepository.findByOrderId(orderId).ifPresent(existingCv -> {
            existingCv.setStatus(CustomerVoucherStatus.AVAILABLE);
            existingCv.setUsedAt(null);
            existingCv.setOrderId(null);
            customerVoucherRepository.save(existingCv);
        });

        // 2. Load & validate the new customer voucher
        CustomerVoucher cv = customerVoucherRepository.findById(customerVoucherId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_VOUCHER_NOT_FOUND));

        if (cv.getStatus() != CustomerVoucherStatus.AVAILABLE) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_ALREADY_USED);
        }

        Voucher voucher = cv.getVoucher();
        if (voucher.getStartAt() != null && Instant.now().isBefore(voucher.getStartAt())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_STARTED_YET);
        }
        if (voucher.getEndAt() != null && Instant.now().isAfter(voucher.getEndAt())) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED);
        }
        if (voucher.getExpiredAt() != null && Instant.now().isAfter(voucher.getExpiredAt())) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED);
        }

        if (order.getSubtotal().compareTo(voucher.getMinBillAmount()) < 0) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_MIN_BILL_NOT_MET);
        }

        // 3. Mark voucher as USED
        cv.setStatus(CustomerVoucherStatus.USED);
        cv.setUsedAt(Instant.now());
        cv.setOrderId(orderId);
        customerVoucherRepository.save(cv);

        // 4. Calculate discount and update order directly
        BigDecimal discountAmount = order.getSubtotal()
                .multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(order.getSubtotal().subtract(discountAmount));
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void removeVoucherFromCurrentOrder() {
        QrSessionData session = currentSession();
        String orderId = session.orderId();
        if (orderId == null || orderId.isBlank()) {
            throw new AppException(ErrorCode.TRACK_NO_ACTIVE_ORDER);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Release voucher
        customerVoucherRepository.findByOrderId(orderId).ifPresent(cv -> {
            cv.setStatus(CustomerVoucherStatus.AVAILABLE);
            cv.setUsedAt(null);
            cv.setOrderId(null);
            customerVoucherRepository.save(cv);
        });

        // Reset discount on order
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(order.getSubtotal());
        orderRepository.save(order);
    }

    // ======== Helpers ========

    private QrSessionData currentSession() {
        String sessionId = AuthUtils.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new AppException(ErrorCode.TQR_SESSION_NOT_FOUND);
        }
        return qrSessionRedisRepository.findSession(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.TQR_SESSION_NOT_FOUND));
    }

    private String resolveOrganizationId(String branchId) {
        OrganizationBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        return branch.getOrganization() != null ? branch.getOrganization().getId() : null;
    }

    private Customer findCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private String generateUniqueVoucherSn() {
        String sn;
        do {
            sn = "V" + UUID.randomUUID().toString().replace("-", "").substring(0, 11).toUpperCase();
        } while (customerVoucherRepository.findByVoucherSn(sn).isPresent());
        return sn;
    }
}
