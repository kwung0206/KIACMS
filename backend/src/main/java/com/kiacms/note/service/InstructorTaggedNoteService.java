package com.kiacms.note.service;

import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.note.dto.request.CreateNoteCommentRequest;
import com.kiacms.note.dto.response.NoteDetailResponse;
import com.kiacms.note.dto.response.TaggedNoteSummaryResponse;
import com.kiacms.note.entity.Note;
import com.kiacms.note.entity.NoteComment;
import com.kiacms.note.repository.NoteCommentRepository;
import com.kiacms.note.repository.NoteRepository;
import com.kiacms.note.repository.NoteTagRepository;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstructorTaggedNoteService {

    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;
    private final NoteCommentRepository noteCommentRepository;
    private final NotificationService notificationService;
    private final NoteResponseMapper noteResponseMapper;

    @Transactional(readOnly = true)
    public List<TaggedNoteSummaryResponse> getTaggedNotes(User instructor) {
        validateInstructor(instructor);
        return noteTagRepository.findAllByTaggedInstructorOrderByCreatedAtDesc(instructor).stream()
                .map(noteResponseMapper::toTaggedSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteDetailResponse getTaggedNoteDetail(UUID noteId, User instructor) {
        validateInstructor(instructor);
        return noteResponseMapper.toDetail(getTaggedNote(noteId, instructor));
    }

    @Transactional
    public NoteDetailResponse createComment(UUID noteId, User instructor, CreateNoteCommentRequest request) {
        validateInstructor(instructor);
        Note note = getTaggedNote(noteId, instructor);

        NoteComment noteComment = NoteComment.builder()
                .note(note)
                .author(instructor)
                .content(request.content().trim())
                .build();

        noteCommentRepository.save(noteComment);
        notificationService.createNoteCommentedNotification(note, note.getAuthor(), instructor);
        return noteResponseMapper.toDetail(note);
    }

    private Note getTaggedNote(UUID noteId, User instructor) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found."));

        if (noteTagRepository.findByNoteAndTaggedInstructor(note, instructor).isEmpty()) {
            throw new AccessDeniedBusinessException("You are not tagged in this note.");
        }

        return note;
    }

    private void validateInstructor(User user) {
        if (user.getRoleType() != RoleType.INSTRUCTOR) {
            throw new AccessDeniedBusinessException("Only instructors can access tagged notes.");
        }
    }
}
