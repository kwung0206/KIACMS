package com.kiacms.project.entity;

import com.kiacms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
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
        name = "project_deletion_histories",
        indexes = {
                @Index(name = "idx_project_deletion_project", columnList = "project_post_id"),
                @Index(name = "idx_project_deletion_deleted_at", columnList = "deleted_at")
        }
)
public class ProjectDeletionHistory extends BaseEntity {

    @Column(name = "project_post_id", nullable = false)
    private UUID projectPostId;

    @Column(name = "project_title", nullable = false, length = 200)
    private String projectTitle;

    @Column(name = "project_owner_id", nullable = false)
    private UUID projectOwnerId;

    @Column(name = "project_owner_name", nullable = false, length = 100)
    private String projectOwnerName;

    @Column(name = "deleted_by_id", nullable = false)
    private UUID deletedById;

    @Column(name = "deleted_by_name", nullable = false, length = 100)
    private String deletedByName;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt;
}
