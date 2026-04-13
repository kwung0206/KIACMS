package com.kiacms.user.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.user.enums.ThemeMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
        name = "user_settings",
        indexes = {
                @Index(name = "idx_user_settings_user", columnList = "user_id")
        }
)
public class UserSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "theme_mode", nullable = false, length = 20)
    private ThemeMode themeMode = ThemeMode.LIGHT;

    @Builder.Default
    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Seoul";

    @Builder.Default
    @Column(name = "locale", nullable = false, length = 20)
    private String locale = "ko-KR";
}
