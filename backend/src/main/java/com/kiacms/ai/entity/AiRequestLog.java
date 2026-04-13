package com.kiacms.ai.entity;

import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.ai.enums.AiReferenceType;
import com.kiacms.ai.enums.AiRequestStatus;
import com.kiacms.global.entity.BaseEntity;
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
        name = "ai_request_logs",
        indexes = {
                @Index(name = "idx_ai_request_logs_requester", columnList = "requester_id, created_at"),
                @Index(name = "idx_ai_request_logs_feature", columnList = "feature_type, status")
        }
)
public class AiRequestLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", nullable = false, length = 50)
    private AiFeatureType featureType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AiRequestStatus status = AiRequestStatus.SUCCESS;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 30)
    private AiReferenceType referenceType = AiReferenceType.FREE_TEXT;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "request_preview", columnDefinition = "text")
    private String requestPreview;

    @Column(name = "response_preview", columnDefinition = "text")
    private String responsePreview;

    @Column(name = "input_token_count")
    private Integer inputTokenCount;

    @Column(name = "output_token_count")
    private Integer outputTokenCount;

    @Column(name = "total_token_count")
    private Integer totalTokenCount;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "completed_at")
    private Instant completedAt;
}
