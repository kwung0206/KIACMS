package com.kiacms.project.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.project.enums.ApplicationStatus;
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
        name = "project_applications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_project_application", columnNames = {"project_position_id", "applicant_id"})
        },
        indexes = {
                @Index(name = "idx_project_applications_status", columnList = "status"),
                @Index(name = "idx_project_applications_applicant", columnList = "applicant_id, status")
        }
)
public class ProjectApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_position_id", nullable = false)
    private ProjectPosition projectPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(name = "motivation", nullable = false, columnDefinition = "text")
    private String motivation;

    @Column(name = "course_history", columnDefinition = "text")
    private String courseHistory;

    @Column(name = "certifications", columnDefinition = "text")
    private String certifications;

    @Column(name = "tech_stack", nullable = false, columnDefinition = "text")
    private String techStack;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "self_introduction", nullable = false, columnDefinition = "text")
    private String selfIntroduction;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(name = "decision_reason", columnDefinition = "text")
    private String decisionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;
}
