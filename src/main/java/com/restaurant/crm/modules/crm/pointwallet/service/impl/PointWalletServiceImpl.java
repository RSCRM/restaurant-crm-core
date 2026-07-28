package com.restaurant.crm.modules.crm.pointwallet.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointHistoryResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;
import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPoint;
import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPointHistory;
import com.restaurant.crm.modules.crm.pointwallet.enums.PointTransactionType;
import com.restaurant.crm.modules.crm.pointwallet.mapper.CustomerPointMapper;
import com.restaurant.crm.modules.crm.pointwallet.repository.CustomerPointHistoryRepository;
import com.restaurant.crm.modules.crm.pointwallet.repository.CustomerPointRepository;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PointWalletServiceImpl implements PointWalletService {

    CustomerPointRepository customerPointRepository;
    CustomerPointHistoryRepository customerPointHistoryRepository;
    CustomerRepository customerRepository;
    OrganizationBranchRepository branchRepository;
    CustomerPointMapper customerPointMapper;

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
    public CustomerPointResponse getWallet(String customerId, String restaurantId) {
        validateBranchAccess(restaurantId);

        CustomerPoint wallet = customerPointRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_POINT_NOT_FOUND));
        return customerPointMapper.toCustomerPointResponse(wallet);
    }

    @Override
    @Transactional
    public CustomerPointResponse initializeWallet(String customerId, String restaurantId) {
        validateBranchAccess(restaurantId);

        CustomerPoint wallet = customerPointRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId)
                .orElseGet(() -> {
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

                    CustomerPoint newWallet = CustomerPoint.builder()
                            .customer(customer)
                            .restaurantId(restaurantId)
                            .currentPoints(0)
                            .lifetimePoints(0)
                            .build();
                    return customerPointRepository.save(newWallet);
                });
        return customerPointMapper.toCustomerPointResponse(wallet);
    }

    @Override
    public PagingResponse<CustomerPointHistoryResponse> getHistory(String customerId, String restaurantId, int page, int size) {
        validateBranchAccess(restaurantId);

        int adjustedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(adjustedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CustomerPointHistory> historyPage = customerPointHistoryRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId, pageable);

        return PagingResponse.<CustomerPointHistoryResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(historyPage.getTotalPages())
                .totalElement(historyPage.getTotalElements())
                .data(historyPage.getContent().stream().map(customerPointMapper::toCustomerPointHistoryResponse).toList())
                .build();
    }


// + point history
    @Override
    @Transactional
    public CustomerPointResponse earnPoints(String customerId, String restaurantId, int points, String orderId) {
        validateBranchAccess(restaurantId);

        CustomerPoint wallet = customerPointRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId)
                .orElseGet(() -> {
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

                    CustomerPoint newWallet = CustomerPoint.builder()
                            .customer(customer)
                            .restaurantId(restaurantId)
                            .currentPoints(0)
                            .lifetimePoints(0)
                            .build();
                    return customerPointRepository.save(newWallet);
                });

        wallet.setCurrentPoints(wallet.getCurrentPoints() + points);
        wallet.setLifetimePoints(wallet.getLifetimePoints() + points);
        customerPointRepository.save(wallet);

        CustomerPointHistory history = CustomerPointHistory.builder()
                .customer(wallet.getCustomer())
                .restaurantId(restaurantId)
                .transactionType(PointTransactionType.EARN)
                .pointsChanged(points)
                .referenceId(orderId)
                .build();
        customerPointHistoryRepository.save(history);

        return customerPointMapper.toCustomerPointResponse(wallet);
    }

// - point history
    @Override
    @Transactional
    public CustomerPointResponse deductPoints(String customerId, String restaurantId, int points, String referenceId) {
        validateBranchAccess(restaurantId);

        CustomerPoint wallet = customerPointRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_POINT_NOT_FOUND));

        if (wallet.getCurrentPoints() < points) {
            throw new AppException(ErrorCode.CUSTOMER_POINT_INSUFFICIENT);
        }

        wallet.setCurrentPoints(wallet.getCurrentPoints() - points);
        customerPointRepository.save(wallet);

        CustomerPointHistory history = CustomerPointHistory.builder()
                .customer(wallet.getCustomer())
                .restaurantId(restaurantId)
                .transactionType(PointTransactionType.REDEEM)
                .pointsChanged(-points)
                .referenceId(referenceId)
                .build();
        customerPointHistoryRepository.save(history);

        return customerPointMapper.toCustomerPointResponse(wallet);
    }
}
