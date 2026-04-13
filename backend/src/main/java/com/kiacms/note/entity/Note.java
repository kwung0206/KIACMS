package com.kiacms.note.entity;

import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.CourseSession;
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
        name = "notes",
        indexes = {
                @Index(name = "idx_notes_author_created", columnList = "author_id, created_at"),
                @Index(name = "idx_notes_course_created", columnList = "course_id, created_at"),
                @Index(name = "idx_notes_session_created", columnList = "course_session_id, created_at")
        }
)
public class Note extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_session_id")
    private CourseSession courseSession;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "last_tagged_at")
    private Instant lastTaggedAt;
}
