package com.restaurant.crm.modules.erp.qr_ordering.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Result of scanning a TABLE QR (uc-c-02): what the customer sees before deciding
 * to open or join a session. Read-only — resolving never writes to Redis.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrResolveResponse {

    String organizationId;
    String branchId;
    String branchName;
    String areaName;
    String tableNumber;
    Integer capacity;
    String tableStatus;

    /** True when the customer may open a new session (table free) or join an existing one. */
    boolean joinable;

    /** True when a session already exists for this table (scan the owner's GROUP QR instead). */
    boolean hasActiveSession;
}
