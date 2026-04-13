package com.kiacms.note.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.user.entity.User;
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
        name = "note_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_note_tag_note_instructor", columnNames = {"note_id", "tagged_instructor_id"})
        },
        indexes = {
                @Index(name = "idx_note_tags_instructor", columnList = "tagged_instructor_id, created_at"),
                @Index(name = "idx_note_tags_note", columnList = "note_id")
        }
)
public class NoteTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagged_instructor_id", nullable = false)
    private User taggedInstructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagged_by_id", nullable = false)
    private User taggedBy;

    @Builder.Default
    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent = false;
}
