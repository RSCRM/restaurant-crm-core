package com.restaurant.crm.modules.crm.loyaltyvoucher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.Voucher;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    List<Voucher> findByBranchIdAndIsActive(String branchId, Short isActive);
    Page<Voucher> findByBranchIdAndIsActive(String branchId, Short isActive, Pageable pageable);
    Page<Voucher> findByBranchId(String branchId, Pageable pageable);
    java.util.Optional<Voucher> findByBranchIdAndVoucherCodeIgnoreCaseAndIsActive(String branchId, String voucherCode, Short isActive);
}
