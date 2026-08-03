package com.restaurant.crm.modules.erp.booking.repository;

import com.restaurant.crm.modules.erp.booking.entity.Booking;
import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByBranchId(String branchId);

    Page<Booking> findByBranchId(String branchId, Pageable pageable);

    List<Booking> findByCustomerId(String customerId);

    Page<Booking> findByCustomerId(String customerId, Pageable pageable);

    Page<Booking> findByCustomerPhone(String phone, Pageable pageable);

    Page<Booking> findByCustomerIdAndBranchId(String customerId, String branchId, Pageable pageable);

    Page<Booking> findByCustomerPhoneAndBranchId(String phone, String branchId, Pageable pageable);

    Page<Booking> findByCustomerIdAndBranch_OrganizationId(String customerId, String organizationId, Pageable pageable);

    Page<Booking> findByCustomerPhoneAndBranch_OrganizationId(String phone, String organizationId, Pageable pageable);

    List<Booking> findByBranchIdAndStatus(String branchId, BookingStatus status);

    List<Booking> findByCustomerIdAndStatus(String customerId, BookingStatus status);

    Optional<Booking> findFirstByTables_IdAndStatusInOrderByBookingTimeAsc(
            String tableId,
            List<BookingStatus> statuses
    );
}
