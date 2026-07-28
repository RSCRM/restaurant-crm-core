package com.restaurant.crm.common.notification.repository;

import com.restaurant.crm.common.notification.entity.NotificationReceipt;
import com.restaurant.crm.common.notification.entity.NotificationReceiptId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface NotificationReceiptRepository extends JpaRepository<NotificationReceipt, NotificationReceiptId> {

    /**
     * Records an acknowledgement, ignoring one that already exists.
     * <p>
     * Native upsert rather than {@code save}: with an assigned composite key JPA would issue a
     * select-then-insert, which two concurrent tabs of the same user turn into a duplicate-key
     * error. {@code ON CONFLICT DO NOTHING} makes the operation naturally idempotent and touches
     * only this table, so readers never contend with the notification row itself.
     */
    @Modifying
    @Query(value = "INSERT INTO notification_receipts (notification_id, recipient_id, status, read_at) "
                   + "VALUES (:notificationId, :recipientId, :status, :readAt) "
                   + "ON CONFLICT (notification_id, recipient_id) DO NOTHING",
           nativeQuery = true)
    int upsert(
            @Param("notificationId") String notificationId,
            @Param("recipientId") String recipientId,
            @Param("status") String status,
            @Param("readAt") Instant readAt
    );
}