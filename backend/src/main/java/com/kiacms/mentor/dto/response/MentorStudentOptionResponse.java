package com.kiacms.mentor.dto.response;

import com.kiacms.user.entity.User;
import java.util.UUID;

public record MentorStudentOptionResponse(
        UUID id,
        String name,
        String email,
        String phoneNumber
) {
    public static MentorStudentOptionResponse from(User user) {
        return new MentorStudentOptionResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
