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
        name = "note_comments",
        indexes = {
                @Index(name = "idx_note_comments_note", columnList = "note_id, created_at"),
                @Index(name = "idx_note_comments_author", columnList = "author_id, created_at")
        }
)
public class NoteComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "edited_at")
    private Instant editedAt;
}
