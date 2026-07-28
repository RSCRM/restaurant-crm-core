package com.restaurant.crm.common.notification.enums;

/**
 * How the audience of a {@link NotificationScope#GROUP} notification is resolved.
 */
public enum NotificationGroupType {

    /** {@code target_key} holds an {@code OrgRole.roleName}. */
    BY_ROLE,

    /**
     * {@code target_key} holds an {@code OrgPermission.permissionName}.
     * Preferred for system-emitted notifications: roles are tenant-defined data and can be renamed,
     * permissions are system constants and cannot.
     */
    BY_PERMISSION
}