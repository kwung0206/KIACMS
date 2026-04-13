package com.kiacms.integration.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.integration.enums.ExternalIntegrationProvider;
import com.kiacms.integration.enums.ExternalIntegrationStatus;
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
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
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
        name = "user_external_integrations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_external_integration", columnNames = {"user_id", "provider"})
        },
        indexes = {
                @Index(name = "idx_user_external_integration_user", columnList = "user_id, provider")
        }
)
public class UserExternalIntegration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private ExternalIntegrationProvider provider;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ExternalIntegrationStatus status = ExternalIntegrationStatus.PENDING;

    @Column(name = "external_workspace_id", length = 255)
    private String externalWorkspaceId;

    @Column(name = "external_workspace_name", length = 100)
    private String externalWorkspaceName;

    @Column(name = "encrypted_secret", columnDefinition = "text")
    private String encryptedSecret;

    @Column(name = "masked_secret_hint", length = 100)
    private String maskedSecretHint;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_sync_message", length = 255)
    private String lastSyncMessage;
}
