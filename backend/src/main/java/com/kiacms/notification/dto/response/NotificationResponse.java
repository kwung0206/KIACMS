package com.kiacms.notification.dto.response;

import com.kiacms.notification.entity.Notification;
import com.kiacms.notification.enums.NotificationTargetType;
import com.kiacms.notification.enums.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        NotificationTargetType targetType,
        UUID targetId,
        String targetUrl,
        boolean isRead,
        Instant createdAt,
        Instant readAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getTargetUrl(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
