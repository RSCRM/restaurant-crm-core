package com.restaurant.crm.modules.erp.notification.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.notification.dto.response.NotificationResponse;
import com.restaurant.crm.modules.erp.notification.entity.Notification;
import com.restaurant.crm.modules.erp.notification.enums.NotificationStatus;
import com.restaurant.crm.modules.erp.notification.enums.NotificationType;
import com.restaurant.crm.modules.erp.notification.mapper.NotificationMapper;
import com.restaurant.crm.modules.erp.notification.repository.NotificationRepository;
import com.restaurant.crm.modules.erp.notification.service.interfaces.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing notifications.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;

    @Override
    @Transactional
    public Notification create(
            String branchId,
            String recipientId,
            String senderId,
            String title,
            String content,
            NotificationType type
    ) {
        Notification notification = Notification.builder()
                .branchId(branchId)
                .recipientId(recipientId)
                .senderId(senderId)
                .title(title)
                .content(content)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    public PagingResponse<NotificationResponse> getNotifications(
            String branchId,
            String employeeId,
            int page,
            int size
    ) {
        // Adjust page to be 0-indexed for Spring Data JPA PageRequest
        int adjustedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(adjustedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Notification> notificationPage = notificationRepository.findByBranchAndRecipient(
                branchId,
                employeeId,
                pageable
        );

        return PagingResponse.<NotificationResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(notificationPage.getTotalPages())
                .totalElement(notificationPage.getTotalElements())
                .data(notificationPage.getContent().stream()
                        .map(notificationMapper::toNotificationResponse)
                        .toList())
                .build();
    }
}
