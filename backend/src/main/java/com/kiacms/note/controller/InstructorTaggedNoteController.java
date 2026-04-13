package com.kiacms.note.controller;

import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.note.dto.request.CreateNoteCommentRequest;
import com.kiacms.note.dto.response.NoteDetailResponse;
import com.kiacms.note.dto.response.TaggedNoteSummaryResponse;
import com.kiacms.note.service.InstructorTaggedNoteService;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructor/tagged-notes")
@RequiredArgsConstructor
public class InstructorTaggedNoteController {

    private final InstructorTaggedNoteService instructorTaggedNoteService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<TaggedNoteSummaryResponse>> getTaggedNotes(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(instructorTaggedNoteService.getTaggedNotes(getCurrentUser(principal)));
    }

    @GetMapping("/{noteId}")
    public ApiResponse<NoteDetailResponse> getTaggedNoteDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID noteId
    ) {
        return ApiResponse.ok(instructorTaggedNoteService.getTaggedNoteDetail(noteId, getCurrentUser(principal)));
    }

    @PostMapping("/{noteId}/comments")
    public ApiResponse<NoteDetailResponse> createComment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID noteId,
            @Valid @RequestBody CreateNoteCommentRequest request
    ) {
        return ApiResponse.ok(instructorTaggedNoteService.createComment(noteId, getCurrentUser(principal), request));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
