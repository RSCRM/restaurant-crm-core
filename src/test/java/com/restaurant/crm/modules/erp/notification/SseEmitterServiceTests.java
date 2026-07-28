package com.restaurant.crm.modules.erp.notification;

import com.restaurant.crm.common.sse.service.impl.SseEmitterServiceImpl;
import com.restaurant.crm.common.notification.dto.response.NotificationResponse;
import com.restaurant.crm.common.notification.enums.NotificationType;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SseEmitterServiceImpl permission-based routing.
 */
@ExtendWith(MockitoExtension.class)
public class SseEmitterServiceTests {

    private SseEmitterServiceImpl sseEmitterService;
    private SecurityContext originalSecurityContext;

    @BeforeEach
    public void setUp() {
        sseEmitterService = new SseEmitterServiceImpl();
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    @Test
    public void testCreateEmitter_AndBroadcastWithPermissionFiltering() {
        String branchId = "branch-1";

        // Setup mock authentication with ORDER_READ authority
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(StartDefinedOrgPermission.ORDER_READ)
        );
        // Cast is required due to wildcard in authorities collection type signature
        when(authentication.getAuthorities()).thenAnswer(inv -> authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Register connections
        SseEmitter waiterEmitter = sseEmitterService.createEmitter(branchId);
        assertNotNull(waiterEmitter);

        // Setup another mock authentication without ORDER_READ (e.g. only TABLE_MANAGE)
        SecurityContext securityContext2 = mock(SecurityContext.class);
        Authentication authentication2 = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities2 = List.of(
                new SimpleGrantedAuthority(StartDefinedOrgPermission.TABLE_MANAGE)
        );
        when(authentication2.getAuthorities()).thenAnswer(inv -> authorities2);
        when(securityContext2.getAuthentication()).thenReturn(authentication2);
        SecurityContextHolder.setContext(securityContext2);

        SseEmitter tableManagerEmitter = sseEmitterService.createEmitter(branchId);
        assertNotNull(tableManagerEmitter);

        // Broadcast notification requiring ORDER_READ
        NotificationResponse notification = NotificationResponse.builder()
                .branchId(branchId)
                .title("Ready to Serve")
                .content("Bàn 01: Phở bò x1 đã xong!")
                .type(NotificationType.READY_TO_SERVE)
                .build();

        // Perform broadcast. Emitters will run without throwing since they are empty connections in tests.
        // It validates that no NullPointerException or SpEL parsing issues occur in the pipeline.
        sseEmitterService.broadcastToBranch(branchId, "READY_TO_SERVE", notification, StartDefinedOrgPermission.ORDER_READ);
    }
}
