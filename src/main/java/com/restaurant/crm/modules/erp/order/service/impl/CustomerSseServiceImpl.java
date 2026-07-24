package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class CustomerSseServiceImpl implements CustomerSseService {

    private final Map<String, List<SseEmitter>> orderEmitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 1800000L; // 30 minutes

    @Override
    public SseEmitter createEmitter(String orderId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        orderEmitters.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>());
        orderEmitters.get(orderId).add(emitter);

        emitter.onCompletion(() -> removeEmitter(orderId, emitter));
        emitter.onTimeout(() -> removeEmitter(orderId, emitter));
        emitter.onError((e) -> removeEmitter(orderId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Customer SSE Connection established for order: " + orderId));
            log.info("Registered customer SSE emitter for order: {}", orderId);
        } catch (IOException e) {
            log.error("Failed to send customer INIT handshake for order: {}", orderId, e);
            removeEmitter(orderId, emitter);
        }

        return emitter;
    }

    @Override
    public void broadcastOrderUpdate(String orderId, Object data) {
        List<SseEmitter> emitters = orderEmitters.get(orderId);
        if (emitters == null || emitters.isEmpty()) {
            log.info("No active customer SSE subscribers for order: {}", orderId);
            return;
        }

        log.info("Broadcasting order update to customer SSE for order: {}", orderId);
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ORDER_ITEM_STATUS_UPDATED")
                        .data(data));
            } catch (Exception e) {
                log.warn("Customer SSE connection lost. Cleaning emitter.");
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }

    private void removeEmitter(String orderId, SseEmitter emitter) {
        List<SseEmitter> emitters = orderEmitters.get(orderId);
        if (emitters != null) {
            emitters.remove(emitter);
            log.info("Removed customer SSE emitter for order: {}. Active subscribers: {}", orderId, emitters.size());
        }
    }
}
