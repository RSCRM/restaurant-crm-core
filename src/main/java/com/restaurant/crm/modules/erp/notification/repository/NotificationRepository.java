package com.restaurant.crm.modules.erp.notification.repository;

import com.restaurant.crm.modules.erp.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Notification entities in the database.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Finds notifications for a specific branch. Supporting optional recipient filter.
     * Notifications targeted to everyone in the branch have null recipientId.
     *
     * @param branchId the ID of the organization branch
     * @param recipientId the ID of the recipient employee
     * @param pageable pagination options
     * @return a page of notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.branchId = :branchId " +
           "AND (n.recipientId = :recipientId OR n.recipientId IS NULL) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByBranchAndRecipient(
            @Param("branchId") String branchId,
            @Param("recipientId") String recipientId,
            Pageable pageable
    );
}
