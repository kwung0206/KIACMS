package com.kiacms.course.dto.response;

import java.util.List;

public record StudentCalendarResponse(
        String timezone,
        List<CalendarEventResponse> events
) {
}
