package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartSseService;
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
public class GroupCartSseServiceImpl implements GroupCartSseService {

    private final Map<String, List<SseEmitter>> sessionEmitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 1800000L; // 30 minutes

    @Override
    public SseEmitter createEmitter(String sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        sessionEmitters.computeIfAbsent(sessionId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError(error -> removeEmitter(sessionId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Group cart SSE connection established for session: " + sessionId));
            log.info("Registered group cart SSE emitter for session: {}", sessionId);
        } catch (IOException exception) {
            log.error("Failed to send group cart INIT handshake for session: {}", sessionId, exception);
            removeEmitter(sessionId, emitter);
        }

        return emitter;
    }

    @Override
    public void broadcast(String sessionId, String eventName, Object data) {
        List<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters == null || emitters.isEmpty()) {
            log.info("No active group cart SSE subscribers for session: {}", sessionId);
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception exception) {
                log.warn("Group cart SSE connection lost for session {}. Cleaning emitter.", sessionId);
                deadEmitters.add(emitter);
            }
        }
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }

    private void removeEmitter(String sessionId, SseEmitter emitter) {
        List<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters != null) {
            emitters.remove(emitter);
            log.info("Removed group cart SSE emitter for session: {}. Active subscribers: {}",
                    sessionId, emitters.size());
        }
    }
}
