package com.kiacms.project.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.project.enums.ContactMethodType;
import com.kiacms.project.enums.ProjectPostStatus;
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
import java.time.LocalDate;
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
        name = "project_posts",
        indexes = {
                @Index(name = "idx_project_posts_owner_status", columnList = "owner_id, status"),
                @Index(name = "idx_project_posts_status_recruit", columnList = "status, recruit_until")
        }
)
public class ProjectPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "goal", nullable = false, columnDefinition = "text")
    private String goal;

    @Column(name = "tech_stack", nullable = false, columnDefinition = "text")
    private String techStack;

    @Column(name = "duration_text", nullable = false, length = 100)
    private String durationText;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_method", nullable = false, length = 30)
    private ContactMethodType contactMethod;

    @Column(name = "contact_value", nullable = false, length = 255)
    private String contactValue;

    @Column(name = "pm_introduction", nullable = false, columnDefinition = "text")
    private String pmIntroduction;

    @Column(name = "pm_background", nullable = false, columnDefinition = "text")
    private String pmBackground;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectPostStatus status = ProjectPostStatus.OPEN;

    @Column(name = "recruit_until")
    private LocalDate recruitUntil;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_id")
    private java.util.UUID deletedById;

    @Column(name = "deletion_reason", columnDefinition = "text")
    private String deletionReason;

    public void markDeleted(User rootUser, String reason) {
        this.status = ProjectPostStatus.DELETED;
        this.deletedAt = Instant.now();
        this.deletedById = rootUser.getId();
        this.deletionReason = reason;
    }
}
