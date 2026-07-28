package com.restaurant.crm.common.notification.constants;

/**
 * JPQL fragments for the notification read path.
 * <p>
 * {@link #VISIBILITY_PREDICATE} is the single written form of "who may see what". Every read query
 * is built from it by concatenation rather than by re-typing the rules, so the feed, the unread
 * count and the single-row lookup cannot drift apart.
 * <p>
 * Its in-memory twin — the predicate used by the SSE push path — lands with the audience work
 * (P2), together with the contract test asserting both forms agree.
 */
public class NotificationQueryConstants {

    private NotificationQueryConstants() {}

    private static final String SCOPE = "com.restaurant.crm.common.notification.enums.NotificationScope.";
    private static final String GROUP_TYPE = "com.restaurant.crm.common.notification.enums.NotificationGroupType.";
    private static final String ROW = "com.restaurant.crm.common.notification.dto.NotificationRow";

    /**
     * Bound parameters: {@code now}, {@code organizationId}, {@code branchId}, {@code employeeId},
     * {@code orgRole}, {@code permissions}.
     * <p>
     * DIRECT notifications intentionally bypass {@code requiredPermission}: if somebody addressed
     * you personally, a functional permission should not hide it from you.
     */
    public static final String VISIBILITY_PREDICATE =
            "     (n.expiresAt IS NULL OR n.expiresAt > :now) "
            + " AND ( n.scope = " + SCOPE + "SYSTEM "
            + "       OR ( n.organizationId = :organizationId "
            + "            AND (    (n.scope = " + SCOPE + "BRANCH AND n.branchId = :branchId) "
            + "                  OR (n.scope = " + SCOPE + "GROUP AND n.branchId = :branchId "
            + "                      AND (    (n.groupType = " + GROUP_TYPE + "BY_ROLE AND n.targetKey = :orgRole) "
            + "                            OR (n.groupType = " + GROUP_TYPE + "BY_PERMISSION AND n.targetKey IN :permissions))) "
            + "                  OR (n.scope = " + SCOPE + "DIRECT AND n.recipientId = :employeeId))))"
            + " AND (    n.requiredPermission IS NULL "
            + "       OR n.scope = " + SCOPE + "DIRECT "
            + "       OR n.requiredPermission IN :permissions) ";

    /** Correlated "no receipt row exists for me" — absence of a receipt is what unread means. */
    private static final String UNREAD_PREDICATE =
            " AND NOT EXISTS (SELECT 1 FROM NotificationReceipt r2 "
            + "                WHERE r2.notificationId = n.id AND r2.recipientId = :employeeId) ";

    private static final String FROM_VISIBLE =
            " FROM Notification n WHERE " + VISIBILITY_PREDICATE;

    /**
     * Ordering lives inside the query rather than in a {@code Pageable}: Spring Data derives the
     * sort alias from the select clause, which is unreliable with a constructor expression.
     * Callers must pass an unsorted {@code Pageable}.
     */
    public static final String FEED_QUERY =
            "SELECT new " + ROW + "(n, r.status) "
            + " FROM Notification n "
            + " LEFT JOIN NotificationReceipt r "
            + "        ON r.notificationId = n.id AND r.recipientId = :employeeId "
            + " WHERE " + VISIBILITY_PREDICATE
            + " ORDER BY n.createdAt DESC";

    public static final String FEED_COUNT_QUERY = "SELECT COUNT(n)" + FROM_VISIBLE;

    public static final String UNREAD_COUNT_QUERY = "SELECT COUNT(n)" + FROM_VISIBLE + UNREAD_PREDICATE;

    public static final String VISIBLE_UNREAD_IDS_QUERY =
            "SELECT n.id" + FROM_VISIBLE + UNREAD_PREDICATE + " ORDER BY n.createdAt DESC";

    public static final String VISIBLE_BY_ID_QUERY =
            "SELECT n" + FROM_VISIBLE + " AND n.id = :id";
}