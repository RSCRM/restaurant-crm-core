package com.restaurant.crm.modules.erp.qr_ordering.enums;

/**
 * Role of a participant inside a QR ordering session (uc-c-02).
 * The first customer that scans the printed TABLE QR becomes the {@link #OWNER};
 * everyone joining afterwards through the GROUP QR is a {@link #MEMBER}.
 * Only the OWNER may finalize the order (BR-CST-GRP-02, enforced in uc-c-05).
 */
public enum SessionMemberRole {
    OWNER,
    MEMBER
}
