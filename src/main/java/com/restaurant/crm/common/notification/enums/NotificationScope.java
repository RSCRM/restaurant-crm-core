package com.restaurant.crm.common.notification.enums;

/**
 * Delivery scope of a notification.
 * <p>
 * The scope drives both the target columns that must be populated (enforced by the
 * {@code ck_notifications_scope_target} database constraint) and the visibility predicate applied
 * when reading.
 */
public enum NotificationScope {

    /** Whole platform. The only scope allowed to cross tenant boundaries. */
    SYSTEM,

    /** Every employee of a single branch. */
    BRANCH,

    /** A group inside a branch, resolved by role or by permission — see {@link NotificationGroupType}. */
    GROUP,

    /** One specific employee. */
    DIRECT
}