package com.restaurant.crm.common.sse.service.impl;

import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Reusable implementation of SseEmitterService.
 * Manages active SSE connections dynamically filtering by branch and client authorities.
 */
@Service
@Slf4j
public class SseEmitterServiceImpl implements SseEmitterService {

    // Thread-safe registry storing connections by branchId
    private final Map<String, List<SseConnection>> branchConnections = new ConcurrentHashMap<>();

    // Timeout value: 30 minutes in milliseconds
    private static final long SSE_TIMEOUT = 1800000L;

    @Getter
    @RequiredArgsConstructor
    private static class SseConnection {
        private final String employeeId;
        private final SseEmitter emitter;
        private final Set<String> permissions;
    }

    @Override
    public SseEmitter createEmitter(String branchId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Resolve current employee context
        String employeeId = null;
        try {
            employeeId = AuthUtils.getEmployeeId();
        } catch (Exception e) {
            log.debug("Unauthenticated SSE subscription request for branch {}", branchId);
        }

        // Resolve authorities/permissions
        Set<String> permissions = new HashSet<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                authentication.getAuthorities().forEach(auth -> permissions.add(auth.getAuthority()));
            }
        } catch (Exception e) {
            log.warn("Failed to extract authorities for SSE client", e);
        }

        SseConnection connection = new SseConnection(employeeId, emitter, permissions);

        // Register connection
        branchConnections.computeIfAbsent(branchId, k -> new CopyOnWriteArrayList<>());
        branchConnections.get(branchId).add(connection);

        // Handle cleanups
        emitter.onCompletion(() -> removeConnection(branchId, connection));
        emitter.onTimeout(() -> removeConnection(branchId, connection));
        emitter.onError((e) -> removeConnection(branchId, connection));

        try {
            // Establish handshake
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("SSE Connection established successfully"));
            log.info("SSE client registered. Employee: {}, Branch: {}", employeeId, branchId);
        } catch (IOException e) {
            log.error("Failed to send INIT handshake. Employee: {}, Branch: {}", employeeId, branchId, e);
            removeConnection(branchId, connection);
        }

        return emitter;
    }

    @Override
    public void broadcastToBranch(String branchId, String eventName, Object data, String requiredPermission) {
        List<SseConnection> connections = branchConnections.get(branchId);
        if (connections == null || connections.isEmpty()) {
            log.info("No active SSE subscribers found for branch: {}", branchId);
            return;
        }

        log.info("Broadcasting event '{}' to branch: {}. Required permission: {}", eventName, branchId, requiredPermission);
        List<SseConnection> deadConnections = new ArrayList<>();

        for (SseConnection connection : connections) {
            // Check permission constraints
            if (requiredPermission != null && !connection.getPermissions().contains(requiredPermission)) {
                log.debug("Skipping push to employee {} - missing permission {}", connection.getEmployeeId(), requiredPermission);
                continue;
            }

            try {
                connection.getEmitter().send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("Connection lost for employee {}. Cleaning connection.", connection.getEmployeeId(), e);
                deadConnections.add(connection);
            }
        }

        // Cleanup stale connections
        if (!deadConnections.isEmpty()) {
            connections.removeAll(deadConnections);
        }
    }

    private void removeConnection(String branchId, SseConnection connection) {
        List<SseConnection> connections = branchConnections.get(branchId);
        if (connections != null) {
            connections.remove(connection);
            log.info("SSE connection removed. Employee: {}, Active connections in branch {}: {}", 
                    connection.getEmployeeId(), branchId, connections.size());
        }
    }
}
