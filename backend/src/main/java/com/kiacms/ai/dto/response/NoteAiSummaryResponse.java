package com.kiacms.ai.dto.response;

import java.util.List;

public record NoteAiSummaryResponse(
        String coreConceptSummary,
        List<String> reviewPoints,
        List<String> questionPoints,
        List<String> easyToMissConcepts
) {
}
