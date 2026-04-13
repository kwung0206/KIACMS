package com.kiacms.project.entity;

import com.kiacms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "project_positions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_project_position_name", columnNames = {"project_post_id", "name"})
        },
        indexes = {
                @Index(name = "idx_project_positions_post", columnList = "project_post_id")
        }
)
public class ProjectPosition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_post_id", nullable = false)
    private ProjectPost projectPost;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "required_skills", columnDefinition = "text")
    private String requiredSkills;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;
}
