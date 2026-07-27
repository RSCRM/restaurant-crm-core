package com.restaurant.crm.modules.crm.loyaltyvoucher.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherRedeemRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherApplicableResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.Voucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus;
import com.restaurant.crm.modules.crm.loyaltyvoucher.mapper.CustomerVoucherMapper;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.CustomerVoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.VoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces.CustomerVoucherService;
import com.restaurant.crm.modules.crm.pointwallet.service.interfaces.PointWalletService;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerVoucherServiceImpl implements CustomerVoucherService {

    CustomerVoucherRepository customerVoucherRepository;
    VoucherRepository voucherRepository;
    CustomerRepository customerRepository;
    OrganizationBranchRepository branchRepository;
    PointWalletService pointWalletService;
    CustomerVoucherMapper customerVoucherMapper;

    private void validateBranchAccess(String targetBranchId) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)) {
            return;
        }
        String actorUserId = AuthUtils.getCurrentUserId();
        if (AuthUtils.getEmployeeId() == null) {
            branchRepository.findByIdAndOrganization_OwnerId(targetBranchId, actorUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));
        } else if (!targetBranchId.equals(AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public CustomerVoucherResponse redeemVoucher(VoucherRedeemRequest request) {
        validateBranchAccess(request.getRestaurantId());

        // 1. Load Voucher
        Voucher voucher = voucherRepository.findById(request.getVoucherId())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (voucher.getIsActive() != 1) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }

        // 2. Load Customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // 3. unique seri for Voucher
        String voucherSn = generateUniqueVoucherSn();

        // 4. - point in customer wallet
        pointWalletService.deductPoints(customer.getId(), request.getRestaurantId(), voucher.getPointsRequired(), voucherSn);

        // 5. save vocher to customer wallet
        CustomerVoucher customerVoucher = CustomerVoucher.builder()
                .customer(customer)
                .restaurantId(request.getRestaurantId())
                .voucher(voucher)
                .voucherSn(voucherSn)
                .status(CustomerVoucherStatus.AVAILABLE)
                .build();

        customerVoucher = customerVoucherRepository.save(customerVoucher);

        return customerVoucherMapper.toCustomerVoucherResponse(customerVoucher);
    }

    @Override
    @Transactional
    public CustomerVoucherResponse giveVoucherDirectly(String customerId, String restaurantId, String voucherId) {
        validateBranchAccess(restaurantId);

        // 1. Load Voucher
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (voucher.getIsActive() != 1) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }

        // 2. Load Customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // 3. unique seri for Voucher
        String voucherSn = generateUniqueVoucherSn();

        // 4. save vocher to customer wallet (FREE)
        CustomerVoucher customerVoucher = CustomerVoucher.builder()
                .customer(customer)
                .restaurantId(restaurantId)
                .voucher(voucher)
                .voucherSn(voucherSn)
                .status(CustomerVoucherStatus.AVAILABLE)
                .build();

        customerVoucher = customerVoucherRepository.save(customerVoucher);

        return customerVoucherMapper.toCustomerVoucherResponse(customerVoucher);
    }

    @Override
    @Transactional
    public CustomerVoucherResponse useVoucher(String customerVoucherId, String orderId, BigDecimal billAmount) {
        // 1. Load CustomerVoucher
        CustomerVoucher customerVoucher = customerVoucherRepository.findById(customerVoucherId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_VOUCHER_NOT_FOUND));

        validateBranchAccess(customerVoucher.getRestaurantId());

        if (customerVoucher.getStatus() != CustomerVoucherStatus.AVAILABLE) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_ALREADY_USED);
        }

        // Check if voucher has expired
        Voucher voucher = customerVoucher.getVoucher();
        if (voucher.getExpiredAt() != null && java.time.Instant.now().isAfter(voucher.getExpiredAt())) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED);
        }

        // 2. minimum bill amount check
        if (billAmount.compareTo(voucher.getMinBillAmount()) < 0) {
            throw new AppException(ErrorCode.CUSTOMER_VOUCHER_MIN_BILL_NOT_MET);
        }

        // 3. update status to USED
        customerVoucher.setStatus(CustomerVoucherStatus.USED);
        customerVoucher.setUsedAt(Instant.now());
        customerVoucher.setOrderId(orderId);

        customerVoucher = customerVoucherRepository.save(customerVoucher);

        return customerVoucherMapper.toCustomerVoucherResponse(customerVoucher);
    }

    @Override
    public List<CustomerVoucherApplicableResponse> getApplicableVouchers(String customerId, String restaurantId, BigDecimal subtotal) {
        validateBranchAccess(restaurantId);

        List<CustomerVoucher> customerVouchers = customerVoucherRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId);
        return customerVouchers.stream().map(cv -> {
            Voucher voucher = cv.getVoucher();
            boolean isExpired = voucher.getExpiredAt() != null && Instant.now().isAfter(voucher.getExpiredAt());
            
            boolean isApplicable = true;
            String reason = null;
            String status = cv.getStatus().name();

            if (isExpired) {
                isApplicable = false;
                reason = "Voucher đã hết hạn sử dụng";
                status = "EXPIRED";
            } else if (cv.getStatus() != CustomerVoucherStatus.AVAILABLE) {
                isApplicable = false;
                reason = "Voucher đã được sử dụng hoặc không còn hiệu lực";
            } else if (subtotal.compareTo(voucher.getMinBillAmount()) < 0) {
                isApplicable = false;
                reason = "Chưa đạt giá trị đơn hàng tối thiểu (Thiếu " + (voucher.getMinBillAmount().subtract(subtotal)) + " VNĐ)";
            }

            return CustomerVoucherApplicableResponse.builder()
                    .customerVoucherId(cv.getId())
                    .voucherSn(cv.getVoucherSn())
                    .title(voucher.getTitle())
                    .discountPercent(voucher.getDiscountPercent())
                    .minBillAmount(voucher.getMinBillAmount())
                    .status(status)
                    .expiredAt(voucher.getExpiredAt())
                    .isApplicable(isApplicable)
                    .reason(reason)
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public void releaseVoucher(String orderId) {
        customerVoucherRepository.findByOrderId(orderId).ifPresent(cv -> {
            // Internal operation, no direct HTTP access, but let's check branch if needed.
            // Since it's triggered internally on order cancel, we can trust it or validate if actor is present.
            if (cv.getStatus() == CustomerVoucherStatus.USED) {
                cv.setStatus(CustomerVoucherStatus.AVAILABLE);
                cv.setUsedAt(null);
                cv.setOrderId(null);
                customerVoucherRepository.save(cv);
            }
        });
    }

    @Override
    public PagingResponse<CustomerVoucherResponse> getCustomerVouchers(String customerId, String restaurantId, String status, int page, int size) {
        validateBranchAccess(restaurantId);

        int adjustedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(adjustedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CustomerVoucher> voucherPage;
        if (status != null && !status.trim().isEmpty()) {
            CustomerVoucherStatus voucherStatus = CustomerVoucherStatus.valueOf(status.toUpperCase());
            voucherPage = customerVoucherRepository.findByCustomerIdAndRestaurantIdAndStatus(customerId, restaurantId, voucherStatus, pageable);
        } else {
            voucherPage = customerVoucherRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId, pageable);
        }

        return PagingResponse.<CustomerVoucherResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(voucherPage.getTotalPages())
                .totalElement(voucherPage.getTotalElements())
                .data(voucherPage.getContent().stream().map(customerVoucherMapper::toCustomerVoucherResponse).toList())
                .build();
    }

    private String generateUniqueVoucherSn() {
        String sn;
        do {
            sn = "V" + UUID.randomUUID().toString().replace("-", "").substring(0, 11).toUpperCase();
        } while (customerVoucherRepository.findByVoucherSn(sn).isPresent());
        return sn;
    }
}
