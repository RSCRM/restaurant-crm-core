package com.restaurant.crm.modules.crm.pointwallet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPointHistory;

@Repository
public interface CustomerPointHistoryRepository extends JpaRepository<CustomerPointHistory, String> {
    Page<CustomerPointHistory> findByCustomerIdAndOrganizationId(String customerId, String organizationId, Pageable pageable);
}
