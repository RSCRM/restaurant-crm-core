package com.restaurant.crm.common.notification.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.notification.constants.NotificationConstants;
import com.restaurant.crm.common.notification.dto.NotificationRow;
import com.restaurant.crm.common.notification.dto.response.NotificationResponse;
import com.restaurant.crm.common.notification.enums.ReceiptStatus;
import com.restaurant.crm.common.notification.mapper.NotificationMapper;
import com.restaurant.crm.common.notification.repository.NotificationReceiptRepository;
import com.restaurant.crm.common.notification.repository.NotificationRepository;
import com.restaurant.crm.common.notification.security.RecipientContext;
import com.restaurant.crm.common.notification.service.interfaces.NotificationQueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Serves the caller's own feed. Tenant and identity come from {@link RecipientContext}, never from
 * the request, and every query runs through the shared visibility predicate.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    NotificationRepository notificationRepository;
    NotificationReceiptRepository notificationReceiptRepository;
    NotificationMapper notificationMapper;

    @Override
    public PagingResponse<NotificationResponse> getFeed(RecipientContext context, int page, int size) {
        int pageIndex = Math.max(0, page - GlobalVariableConstant.PAGE_SIZE_INDEX);
        // Unsorted on purpose: the ordering is part of the query (see NotificationQueryConstants).
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<NotificationRow> rows = notificationRepository.findFeed(
                context.organizationId(),
                context.branchId(),
                context.employeeId(),
                context.orgRole(),
                context.permissions(),
                Instant.now(),
                pageable
        );

        return PagingResponse.<NotificationResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(rows.getTotalPages())
                .totalElement(rows.getTotalElements())
                .data(rows.getContent().stream()
                        .map(row -> notificationMapper.toNotificationResponse(
                                row.notification(), row.receiptStatus()))
                        .toList())
                .build();
    }

    @Override
    public long countUnread(RecipientContext context) {
        return notificationRepository.countUnread(
                context.organizationId(),
                context.branchId(),
                context.employeeId(),
                context.orgRole(),
                context.permissions(),
                Instant.now()
        );
    }

    @Override
    @Transactional
    public void markRead(RecipientContext context, String notificationId) {
        // Resolving through the visibility predicate keeps the caller from acknowledging — and
        // thereby probing the existence of — a notification addressed to somebody else.
        notificationRepository.findVisibleById(
                notificationId,
                context.organizationId(),
                context.branchId(),
                context.employeeId(),
                context.orgRole(),
                context.permissions(),
                Instant.now()
        ).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notificationReceiptRepository.upsert(
                notificationId,
                context.employeeId(),
                ReceiptStatus.READ.name(),
                Instant.now()
        );
    }

    @Override
    @Transactional
    public int markAllRead(RecipientContext context) {
        Instant now = Instant.now();

        // Capped rather than unbounded: the ids are fetched through the same predicate as the feed,
        // so there is exactly one written definition of visibility. A caller sitting on more than
        // the cap acknowledges the newest ones and can repeat the call.
        List<String> ids = notificationRepository.findVisibleUnreadIds(
                context.organizationId(),
                context.branchId(),
                context.employeeId(),
                context.orgRole(),
                context.permissions(),
                now,
                PageRequest.of(0, NotificationConstants.MARK_ALL_READ_LIMIT)
        );

        for (String id : ids) {
            notificationReceiptRepository.upsert(id, context.employeeId(), ReceiptStatus.READ.name(), now);
        }
        return ids.size();
    }
}