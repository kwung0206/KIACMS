package com.kiacms.notification.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.notification.enums.NotificationTargetType;
import com.kiacms.notification.enums.NotificationType;
import com.kiacms.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_read", columnList = "recipient_id, is_read, created_at")
        }
)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private NotificationTargetType targetType = NotificationTargetType.DASHBOARD;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    public void markAsRead() {
        if (this.isRead) {
            return;
        }
        this.isRead = true;
        this.readAt = Instant.now();
    }
}
