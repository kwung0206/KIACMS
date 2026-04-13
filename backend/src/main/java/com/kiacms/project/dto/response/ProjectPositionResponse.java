package com.kiacms.project.dto.response;

import com.kiacms.project.entity.ProjectPosition;
import java.util.UUID;

public record ProjectPositionResponse(
        UUID id,
        String name,
        String description,
        String requiredSkills,
        Integer capacity,
        long acceptedCount,
        long remainingSlots
) {
    public static ProjectPositionResponse from(ProjectPosition position, long acceptedCount) {
        return new ProjectPositionResponse(
                position.getId(),
                position.getName(),
                position.getDescription(),
                position.getRequiredSkills(),
                position.getCapacity(),
                acceptedCount,
                Math.max(position.getCapacity() - acceptedCount, 0)
        );
    }
}
