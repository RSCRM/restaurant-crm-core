package com.restaurant.crm.common.notification.enums;

import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Catalogue of notification types together with their default audience policy.
 * <p>
 * Keeping scope, group type, required permission and priority here means a business module only
 * says <em>what happened</em>; it never decides <em>who is allowed to see it</em>. Changing the
 * audience of a type is therefore a one-line change in a single place.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum NotificationType {

    /** A dish finished preparation and waits to be served — visible to whoever handles orders. */
    READY_TO_SERVE(
            NotificationScope.GROUP,
            NotificationGroupType.BY_PERMISSION,
            StartDefinedOrgPermission.ORDER_READ,
            NotificationPriority.HIGH
    );

    NotificationScope defaultScope;

    /** Only meaningful when {@link #defaultScope} is {@link NotificationScope#GROUP}. */
    NotificationGroupType groupType;

    /**
     * Minimum authority required to see notifications of this type. {@code null} means every
     * employee inside the scope can see it.
     */
    String requiredPermission;

    NotificationPriority priority;

    /**
     * Default group target. For {@link NotificationGroupType#BY_PERMISSION} the audience is exactly
     * "whoever holds the required permission", so the target key is that permission.
     * {@link NotificationGroupType#BY_ROLE} types have no sensible default — the emitter supplies
     * the role name.
     */
    public String getDefaultTargetKey() {
        return groupType == NotificationGroupType.BY_PERMISSION ? requiredPermission : null;
    }
}