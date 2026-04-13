package com.kiacms.course.dto.response;

import com.kiacms.user.entity.User;
import java.util.UUID;

public record InstructorOptionResponse(
        UUID id,
        String name,
        String email
) {
    public static InstructorOptionResponse from(User user) {
        return new InstructorOptionResponse(user.getId(), user.getName(), user.getEmail());
    }
}
