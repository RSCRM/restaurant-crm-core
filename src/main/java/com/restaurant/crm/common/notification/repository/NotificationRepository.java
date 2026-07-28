package com.restaurant.crm.common.notification.repository;

import com.restaurant.crm.common.notification.constants.NotificationQueryConstants;
import com.restaurant.crm.common.notification.dto.NotificationRow;
import com.restaurant.crm.common.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Reads always go through the shared visibility predicate and always take an organization id.
 * <p>
 * {@code findById} is inherited from {@link JpaRepository} but must not be used by services: a
 * lookup that ignores the tenant is exactly how a notification from another organization ends up on
 * somebody's screen. Use {@link #findVisibleById} instead.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * One page of the caller's feed, newest first, with their acknowledgement joined in.
     *
     * @param pageable must be unsorted — ordering is fixed by the query
     */
    @Query(value = NotificationQueryConstants.FEED_QUERY,
           countQuery = NotificationQueryConstants.FEED_COUNT_QUERY)
    Page<NotificationRow> findFeed(
            @Param("organizationId") String organizationId,
            @Param("branchId") String branchId,
            @Param("employeeId") String employeeId,
            @Param("orgRole") String orgRole,
            @Param("permissions") Collection<String> permissions,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query(NotificationQueryConstants.UNREAD_COUNT_QUERY)
    long countUnread(
            @Param("organizationId") String organizationId,
            @Param("branchId") String branchId,
            @Param("employeeId") String employeeId,
            @Param("orgRole") String orgRole,
            @Param("permissions") Collection<String> permissions,
            @Param("now") Instant now
    );

    /** Ids of visible, still-unacknowledged notifications — newest first, capped by the pageable. */
    @Query(NotificationQueryConstants.VISIBLE_UNREAD_IDS_QUERY)
    List<String> findVisibleUnreadIds(
            @Param("organizationId") String organizationId,
            @Param("branchId") String branchId,
            @Param("employeeId") String employeeId,
            @Param("orgRole") String orgRole,
            @Param("permissions") Collection<String> permissions,
            @Param("now") Instant now,
            Pageable pageable
    );

    /**
     * Single notification, but only if the caller may see it. Callers turn an empty result into
     * 404 rather than 403: a 403 confirms the row exists in somebody else's tenant.
     */
    @Query(NotificationQueryConstants.VISIBLE_BY_ID_QUERY)
    Optional<Notification> findVisibleById(
            @Param("id") String id,
            @Param("organizationId") String organizationId,
            @Param("branchId") String branchId,
            @Param("employeeId") String employeeId,
            @Param("orgRole") String orgRole,
            @Param("permissions") Collection<String> permissions,
            @Param("now") Instant now
    );

    /** Idempotency lookup for at-least-once emitters. */
    Optional<Notification> findByOrganizationIdAndDedupeKey(String organizationId, String dedupeKey);
}