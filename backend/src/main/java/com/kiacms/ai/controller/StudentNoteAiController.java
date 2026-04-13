package com.kiacms.ai.controller;

import com.kiacms.ai.dto.response.NoteAiSummaryResponse;
import com.kiacms.ai.service.NoteAiService;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/ai")
@RequiredArgsConstructor
public class StudentNoteAiController {

    private final NoteAiService noteAiService;
    private final UserRepository userRepository;

    @PostMapping("/notes/{noteId}/summary")
    public ApiResponse<NoteAiSummaryResponse> summarizeNote(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID noteId
    ) {
        return ApiResponse.ok(noteAiService.summarizeNote(noteId, getCurrentUser(principal)));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
