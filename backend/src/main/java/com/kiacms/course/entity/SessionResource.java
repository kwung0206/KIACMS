package com.kiacms.course.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
        name = "session_resources",
        indexes = {
                @Index(name = "idx_session_resources_session", columnList = "course_session_id")
        }
)
public class SessionResource extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_session_id", nullable = false, unique = true)
    private CourseSession courseSession;

    @Column(name = "zoom_link", length = 500)
    private String zoomLink;

    @Column(name = "recording_link", length = 500)
    private String recordingLink;

    @Column(name = "summary_link", length = 500)
    private String summaryLink;

    @Column(name = "additional_notice", columnDefinition = "text")
    private String additionalNotice;

    @Column(name = "zoom_link_updated_at")
    private Instant zoomLinkUpdatedAt;

    @Column(name = "recording_link_updated_at")
    private Instant recordingLinkUpdatedAt;

    @Column(name = "summary_link_updated_at")
    private Instant summaryLinkUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by_id")
    private User lastUpdatedBy;
}
