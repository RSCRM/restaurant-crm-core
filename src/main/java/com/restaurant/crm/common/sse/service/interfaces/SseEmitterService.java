package com.restaurant.crm.common.sse.service.interfaces;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Reusable common service interface for managing Server-Sent Events (SSE) emitters.
 * Enables server-side connection registry and branch/permission-based real-time broadcasting.
 */
public interface SseEmitterService {

    /**
     * Registers a new SSE connection for a specific branch and returns the emitter.
     * Resolves metadata (employee ID, authorities) dynamically from the authentication context.
     *
     * @param branchId the ID of the organization branch
     * @return the created SseEmitter
     */
    SseEmitter createEmitter(String branchId);

    /**
     * Broadcasts an event to active subscribers in a specific branch who hold the required permission.
     *
     * @param branchId the ID of the organization branch
     * @param eventName the name of the SSE event (e.g. "READY_TO_SERVE", "BOOKING_CREATED")
     * @param data the payload of the event (automatically serialized to JSON)
     * @param requiredPermission the permission string required to receive this event (nullable)
     */
    void broadcastToBranch(String branchId, String eventName, Object data, String requiredPermission);
}
