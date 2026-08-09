package com.restaurant.crm.modules.crm.loyaltyvoucher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, String> {
    Optional<CustomerVoucher> findByVoucherSn(String voucherSn);
    List<CustomerVoucher> findByCustomerIdAndBranchIdAndStatus(String customerId, String branchId, CustomerVoucherStatus status);
    Page<CustomerVoucher> findByCustomerIdAndBranchIdAndStatus(String customerId, String branchId, CustomerVoucherStatus status, Pageable pageable);
    Page<CustomerVoucher> findByCustomerIdAndBranchId(String customerId, String branchId, Pageable pageable);
    Optional<CustomerVoucher> findByOrderId(String orderId);
    List<CustomerVoucher> findByCustomerIdAndBranchId(String customerId, String branchId);
    boolean existsByCustomerIdAndVoucherId(String customerId, String voucherId);
    long countByVoucherIdAndStatus(String voucherId, CustomerVoucherStatus status);
}
