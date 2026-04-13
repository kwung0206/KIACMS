package com.kiacms.course.dto.response;

import com.kiacms.course.entity.SessionResource;
import java.time.Instant;

public record SessionResourceResponse(
        String zoomLink,
        String recordingLink,
        String summaryLink,
        String additionalNotice,
        Instant zoomLinkUpdatedAt,
        Instant recordingLinkUpdatedAt,
        Instant summaryLinkUpdatedAt
) {
    public static SessionResourceResponse from(SessionResource resource) {
        if (resource == null) {
            return new SessionResourceResponse(null, null, null, null, null, null, null);
        }

        return new SessionResourceResponse(
                resource.getZoomLink(),
                resource.getRecordingLink(),
                resource.getSummaryLink(),
                resource.getAdditionalNotice(),
                resource.getZoomLinkUpdatedAt(),
                resource.getRecordingLinkUpdatedAt(),
                resource.getSummaryLinkUpdatedAt()
        );
    }
}
