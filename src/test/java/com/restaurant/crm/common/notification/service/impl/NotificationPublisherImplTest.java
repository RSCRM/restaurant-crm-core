package com.restaurant.crm.common.notification.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.notification.dto.NotificationCommand;
import com.restaurant.crm.common.notification.entity.Notification;
import com.restaurant.crm.common.notification.enums.NotificationGroupType;
import com.restaurant.crm.common.notification.enums.NotificationPriority;
import com.restaurant.crm.common.notification.enums.NotificationScope;
import com.restaurant.crm.common.notification.enums.NotificationSenderType;
import com.restaurant.crm.common.notification.enums.NotificationType;
import com.restaurant.crm.common.notification.repository.NotificationRepository;
import com.restaurant.crm.common.notification.service.interfaces.BranchTenantResolver;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the write path: scope/target integrity and tenant resolution.
 */
@ExtendWith(MockitoExtension.class)
public class NotificationPublisherImplTest {

    private static final String ORGANIZATION_ID = "org-1";
    private static final String BRANCH_ID = "branch-1";
    private static final String EMPLOYEE_ID = "employee-1";

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    BranchTenantResolver branchTenantResolver;

    @InjectMocks
    NotificationPublisherImpl notificationPublisher;

    @Test
    public void publish_groupScope_appliesAudienceFromTypeCatalogue() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationPublisher.publish(NotificationCommand.from(NotificationType.READY_TO_SERVE)
                .organizationId(ORGANIZATION_ID)
                .branchId(BRANCH_ID)
                .senderId(EMPLOYEE_ID)
                .title("Dish Ready to Serve")
                .content("Khu A - Bàn 01: Phở Bò x2 is ready to serve!")
                .build());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(NotificationScope.GROUP, saved.getScope());
        assertEquals(NotificationGroupType.BY_PERMISSION, saved.getGroupType());
        assertEquals(StartDefinedOrgPermission.ORDER_READ, saved.getTargetKey());
        assertEquals(StartDefinedOrgPermission.ORDER_READ, saved.getRequiredPermission());
        assertEquals(NotificationPriority.HIGH, saved.getPriority());
        assertEquals(NotificationSenderType.SYSTEM, saved.getSenderType());
        assertEquals(ORGANIZATION_ID, saved.getOrganizationId());
        assertNull(saved.getRecipientId());
    }

    @Test
    public void publish_withKnownOrganization_doesNotHitTheResolver() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationPublisher.publish(NotificationCommand.from(NotificationType.READY_TO_SERVE)
                .organizationId(ORGANIZATION_ID)
                .branchId(BRANCH_ID)
                .title("t")
                .content("c")
                .build());

        verify(branchTenantResolver, never()).resolveOrganizationId(anyString());
    }

    @Test
    public void publish_withoutOrganization_resolvesItFromTheBranch() {
        when(branchTenantResolver.resolveOrganizationId(BRANCH_ID)).thenReturn(ORGANIZATION_ID);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationPublisher.publish(NotificationCommand.from(NotificationType.READY_TO_SERVE)
                .branchId(BRANCH_ID)
                .title("t")
                .content("c")
                .build());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(ORGANIZATION_ID, captor.getValue().getOrganizationId());
    }

    @Test
    public void publish_systemScopeCarryingATenant_isRejected() {
        // Stripping the tenant instead of rejecting would silently turn a mis-addressed
        // notification into a platform-wide broadcast.
        NotificationCommand command = NotificationCommand.builder()
                .scope(NotificationScope.SYSTEM)
                .organizationId(ORGANIZATION_ID)
                .type(NotificationType.READY_TO_SERVE)
                .title("t")
                .content("c")
                .build();

        AppException exception = assertThrows(AppException.class, () -> notificationPublisher.publish(command));
        assertEquals(ErrorCode.NOTIFICATION_INVALID_TARGET, exception.getErrorCode());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    public void publish_groupScopeWithoutTargetKey_isRejected() {
        NotificationCommand command = NotificationCommand.builder()
                .scope(NotificationScope.GROUP)
                .organizationId(ORGANIZATION_ID)
                .branchId(BRANCH_ID)
                .groupType(NotificationGroupType.BY_ROLE)
                .type(NotificationType.READY_TO_SERVE)
                .title("t")
                .content("c")
                .build();

        assertThrows(AppException.class, () -> notificationPublisher.publish(command));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    public void publish_directScopeWithoutRecipient_isRejected() {
        NotificationCommand command = NotificationCommand.builder()
                .scope(NotificationScope.DIRECT)
                .organizationId(ORGANIZATION_ID)
                .branchId(BRANCH_ID)
                .type(NotificationType.READY_TO_SERVE)
                .title("t")
                .content("c")
                .build();

        assertThrows(AppException.class, () -> notificationPublisher.publish(command));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    public void publish_knownDedupeKey_returnsTheExistingRowWithoutWriting() {
        Notification existing = Notification.builder().id("notification-1").build();
        when(notificationRepository.findByOrganizationIdAndDedupeKey(ORGANIZATION_ID, "order-1:READY"))
                .thenReturn(Optional.of(existing));

        Notification result = notificationPublisher.publish(
                NotificationCommand.from(NotificationType.READY_TO_SERVE)
                        .organizationId(ORGANIZATION_ID)
                        .branchId(BRANCH_ID)
                        .dedupeKey("order-1:READY")
                        .title("t")
                        .content("c")
                        .build());

        assertSame(existing, result);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    public void publish_blankContent_isRejected() {
        NotificationCommand command = NotificationCommand.from(NotificationType.READY_TO_SERVE)
                .organizationId(ORGANIZATION_ID)
                .branchId(BRANCH_ID)
                .title("t")
                .content("  ")
                .build();

        assertThrows(AppException.class, () -> notificationPublisher.publish(command));
        verify(notificationRepository, never()).save(any());
    }
}
