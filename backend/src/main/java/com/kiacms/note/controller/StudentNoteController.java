package com.kiacms.note.controller;

import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.note.dto.request.CreateNoteRequest;
import com.kiacms.note.dto.request.TagInstructorsRequest;
import com.kiacms.note.dto.request.UpdateNoteRequest;
import com.kiacms.note.dto.response.NoteDetailResponse;
import com.kiacms.note.dto.response.NoteSummaryResponse;
import com.kiacms.note.service.StudentNoteService;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/notes")
@RequiredArgsConstructor
public class StudentNoteController {

    private final StudentNoteService studentNoteService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<NoteSummaryResponse>> getMyNotes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID courseSessionId
    ) {
        return ApiResponse.ok(studentNoteService.getMyNotes(getCurrentUser(principal), courseId, courseSessionId));
    }

    @GetMapping("/{noteId}")
    public ApiResponse<NoteDetailResponse> getMyNoteDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID noteId
    ) {
        return ApiResponse.ok(studentNoteService.getMyNoteDetail(noteId, getCurrentUser(principal)));
    }

    @PostMapping
    public ApiResponse<NoteDetailResponse> createNote(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateNoteRequest request
    ) {
        return ApiResponse.ok(studentNoteService.createNote(request, getCurrentUser(principal)));
    }

    @PutMapping("/{noteId}")
    public ApiResponse<NoteDetailResponse> updateNote(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateNoteRequest request
    ) {
        return ApiResponse.ok(studentNoteService.updateNote(noteId, request, getCurrentUser(principal)));
    }

    @PostMapping("/{noteId}/tags")
    public ApiResponse<NoteDetailResponse> addTags(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID noteId,
            @Valid @RequestBody TagInstructorsRequest request
    ) {
        return ApiResponse.ok(studentNoteService.addTags(noteId, request, getCurrentUser(principal)));
    }

    @DeleteMapping("/{noteId}")
    public ApiResponse<Void> deleteNote(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID noteId
    ) {
        studentNoteService.deleteNote(noteId, getCurrentUser(principal));
        return ApiResponse.ok();
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
