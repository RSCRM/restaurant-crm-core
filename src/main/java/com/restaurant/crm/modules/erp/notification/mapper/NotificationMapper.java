package com.restaurant.crm.modules.erp.notification.mapper;

import com.restaurant.crm.modules.erp.notification.dto.response.NotificationResponse;
import com.restaurant.crm.modules.erp.notification.entity.Notification;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting Notification entities to DTO responses.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toNotificationResponse(Notification notification);
}
