package com.restaurant.crm.common.notification.mapper;

import com.restaurant.crm.common.notification.dto.response.NotificationResponse;
import com.restaurant.crm.common.notification.entity.Notification;
import com.restaurant.crm.common.notification.enums.ReceiptStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps notification entities to their client-facing representation.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /** Without recipient context: {@code readStatus} stays null. Used by the push path. */
    @Mapping(target = "readStatus", ignore = true)
    NotificationResponse toNotificationResponse(Notification notification);

    /** Feed variant, carrying the calling employee's acknowledgement. */
    @Mapping(target = "readStatus", source = "receiptStatus")
    NotificationResponse toNotificationResponse(Notification notification, ReceiptStatus receiptStatus);
}