package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.enums.QrSessionStatus;
import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.model.QrSessionMember;
import com.restaurant.crm.modules.erp.order.repository.GroupCartRedisRepository;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BR-CST-GRP-04: when the OWNER is idle past the timeout, the earliest-joined still-active member
 * becomes OWNER on the next request; an active owner is never displaced.
 */
class GroupCartHostHandoverTest {

    private static final String SESSION = "session-1";
    private static final String BRANCH = "branch-1";

    private GroupCartServiceImpl service(GroupCartRedisRepository cartRepo, QrSessionRedisRepository sessionRepo,
                                         GroupCartSseService sse) {
        return new GroupCartServiceImpl(cartRepo, sessionRepo, mock(ProductRepository.class),
                mock(ComboRepository.class), mock(ModifierOptionRepository.class), sse, mock(OrderService.class));
    }

    @Test
    void idleOwnerIsReplacedByEarliestActiveMember() {
        GroupCartRedisRepository cartRepo = mock(GroupCartRedisRepository.class);
        QrSessionRedisRepository sessionRepo = mock(QrSessionRedisRepository.class);
        GroupCartSseService sse = mock(GroupCartSseService.class);

        Instant now = Instant.now();
        QrSessionMember idleOwner = member("owner", SessionMemberRole.OWNER, now.minusSeconds(1000), now.minusSeconds(700));
        QrSessionMember earlyMember = member("m1", SessionMemberRole.MEMBER, now.minusSeconds(900), now.minusSeconds(10));
        QrSessionMember lateMember = member("m2", SessionMemberRole.MEMBER, now.minusSeconds(800), now.minusSeconds(10));

        when(sessionRepo.findSession(SESSION)).thenReturn(Optional.of(session("owner")));
        when(sessionRepo.getMembers(SESSION)).thenReturn(List.of(idleOwner, earlyMember, lateMember));
        when(sessionRepo.getMember(eq(SESSION), eq("m1")))
                .thenReturn(Optional.of(member("m1", SessionMemberRole.OWNER, now.minusSeconds(900), now)));
        when(cartRepo.getItems(SESSION)).thenReturn(List.of());

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getSessionId).thenReturn(SESSION);
            auth.when(AuthUtils::getDeviceId).thenReturn("m1");

            service(cartRepo, sessionRepo, sse).getCart();

            verify(sessionRepo).saveMember(eq(SESSION),
                    argThat(m -> "m1".equals(m.deviceId()) && m.role() == SessionMemberRole.OWNER), anyLong());
            verify(sessionRepo).saveMember(eq(SESSION),
                    argThat(m -> "owner".equals(m.deviceId()) && m.role() == SessionMemberRole.MEMBER), anyLong());
            verify(sessionRepo).updateOwnerDeviceId(SESSION, "m1");
            verify(sse).broadcast(eq(SESSION), eq("SESSION_HOST_CHANGED"), org.mockito.ArgumentMatchers.any());
        }
    }

    @Test
    void activeOwnerIsNotReplaced() {
        GroupCartRedisRepository cartRepo = mock(GroupCartRedisRepository.class);
        QrSessionRedisRepository sessionRepo = mock(QrSessionRedisRepository.class);
        GroupCartSseService sse = mock(GroupCartSseService.class);

        Instant now = Instant.now();
        QrSessionMember activeOwner = member("owner", SessionMemberRole.OWNER, now.minusSeconds(1000), now.minusSeconds(5));
        QrSessionMember someMember = member("m1", SessionMemberRole.MEMBER, now.minusSeconds(900), now.minusSeconds(5));

        when(sessionRepo.findSession(SESSION)).thenReturn(Optional.of(session("owner")));
        when(sessionRepo.getMembers(SESSION)).thenReturn(List.of(activeOwner, someMember));
        when(sessionRepo.getMember(eq(SESSION), eq("owner"))).thenReturn(Optional.of(activeOwner));
        when(cartRepo.getItems(SESSION)).thenReturn(List.of());

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getSessionId).thenReturn(SESSION);
            auth.when(AuthUtils::getDeviceId).thenReturn("owner");

            service(cartRepo, sessionRepo, sse).getCart();

            verify(sessionRepo, never()).updateOwnerDeviceId(eq(SESSION), org.mockito.ArgumentMatchers.anyString());
            verify(sse, never()).broadcast(eq(SESSION), eq("SESSION_HOST_CHANGED"), org.mockito.ArgumentMatchers.any());
        }
    }

    private QrSessionData session(String ownerDeviceId) {
        return new QrSessionData(SESSION, "org-1", BRANCH, "table-1", ownerDeviceId,
                null, "0900000000", null, QrSessionStatus.OPEN, Instant.now());
    }

    private QrSessionMember member(String deviceId, SessionMemberRole role, Instant joinedAt, Instant lastSeenAt) {
        return new QrSessionMember(deviceId, role, null, null, joinedAt, lastSeenAt);
    }
}
