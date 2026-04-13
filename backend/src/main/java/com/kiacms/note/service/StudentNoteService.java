package com.kiacms.note.service;

import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.CourseSession;
import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.enums.EnrollmentStatus;
import com.kiacms.course.repository.CourseRepository;
import com.kiacms.course.repository.CourseSessionRepository;
import com.kiacms.course.repository.EnrollmentRepository;
import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.note.dto.request.CreateNoteRequest;
import com.kiacms.note.dto.request.TagInstructorsRequest;
import com.kiacms.note.dto.request.UpdateNoteRequest;
import com.kiacms.note.dto.response.NoteDetailResponse;
import com.kiacms.note.dto.response.NoteSummaryResponse;
import com.kiacms.note.entity.Note;
import com.kiacms.note.entity.NoteTag;
import com.kiacms.note.repository.NoteCommentRepository;
import com.kiacms.note.repository.NoteRepository;
import com.kiacms.note.repository.NoteTagRepository;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import com.kiacms.user.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentNoteService {

    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;
    private final NoteCommentRepository noteCommentRepository;
    private final CourseRepository courseRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final NoteResponseMapper noteResponseMapper;

    @Transactional(readOnly = true)
    public List<NoteSummaryResponse> getMyNotes(User student, UUID courseId, UUID courseSessionId) {
        validateStudent(student);

        List<Note> notes;
        if (courseSessionId != null) {
            CourseSession courseSession = courseSessionRepository.findById(courseSessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course session not found."));
            if (courseId != null && !courseSession.getCourse().getId().equals(courseId)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "The selected session does not belong to the selected course.");
            }
            notes = noteRepository.findAllByAuthorAndCourseSessionOrderByCreatedAtDesc(student, courseSession);
        } else if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
            notes = noteRepository.findAllByAuthorAndCourseOrderByCreatedAtDesc(student, course);
        } else {
            notes = noteRepository.findAllByAuthorOrderByCreatedAtDesc(student);
        }

        return notes.stream()
                .map(noteResponseMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteDetailResponse getMyNoteDetail(UUID noteId, User student) {
        validateStudent(student);
        return noteResponseMapper.toDetail(getOwnedNote(noteId, student));
    }

    @Transactional
    public NoteDetailResponse createNote(CreateNoteRequest request, User student) {
        validateStudent(student);

        Course course = getAccessibleCourse(student, request.courseId());
        CourseSession courseSession = resolveCourseSession(course, request.courseSessionId());

        Note note = Note.builder()
                .author(student)
                .course(course)
                .courseSession(courseSession)
                .title(request.title().trim())
                .content(request.content().trim())
                .build();

        Note savedNote = noteRepository.save(note);
        addInstructorTags(savedNote, student, request.taggedInstructorIds());
        return noteResponseMapper.toDetail(savedNote);
    }

    @Transactional
    public NoteDetailResponse updateNote(UUID noteId, UpdateNoteRequest request, User student) {
        validateStudent(student);

        Note note = getOwnedNote(noteId, student);
        Course course = getAccessibleCourse(student, request.courseId());
        CourseSession courseSession = resolveCourseSession(course, request.courseSessionId());

        note.setCourse(course);
        note.setCourseSession(courseSession);
        note.setTitle(request.title().trim());
        note.setContent(request.content().trim());
        removeInvalidTags(note);

        return noteResponseMapper.toDetail(note);
    }

    @Transactional
    public NoteDetailResponse addTags(UUID noteId, TagInstructorsRequest request, User student) {
        validateStudent(student);
        Note note = getOwnedNote(noteId, student);
        addInstructorTags(note, student, request.instructorIds());
        return noteResponseMapper.toDetail(note);
    }

    @Transactional
    public void deleteNote(UUID noteId, User student) {
        validateStudent(student);
        Note note = getOwnedNote(noteId, student);
        noteCommentRepository.deleteAllByNote(note);
        noteTagRepository.deleteAllByNote(note);
        noteRepository.delete(note);
    }

    private Note getOwnedNote(UUID noteId, User student) {
        return noteRepository.findByIdAndAuthor(noteId, student)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found."));
    }

    private Course getAccessibleCourse(User student, UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new AccessDeniedBusinessException("You are not enrolled in this course."));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new AccessDeniedBusinessException("You cannot write notes for this course.");
        }

        return course;
    }

    private CourseSession resolveCourseSession(Course course, UUID courseSessionId) {
        if (courseSessionId == null) {
            return null;
        }

        CourseSession courseSession = courseSessionRepository.findById(courseSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Course session not found."));

        if (!courseSession.getCourse().getId().equals(course.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "The selected session does not belong to the selected course.");
        }

        return courseSession;
    }

    private void addInstructorTags(Note note, User student, List<UUID> instructorIds) {
        Set<UUID> normalizedInstructorIds = normalizeInstructorIds(instructorIds);
        if (normalizedInstructorIds.isEmpty()) {
            return;
        }

        Set<UUID> existingInstructorIds = noteTagRepository.findAllByNoteOrderByCreatedAtAsc(note).stream()
                .map(noteTag -> noteTag.getTaggedInstructor().getId())
                .collect(Collectors.toSet());

        List<NoteTag> newTags = new ArrayList<>();
        for (UUID instructorId : normalizedInstructorIds) {
            if (existingInstructorIds.contains(instructorId)) {
                continue;
            }

            User instructor = resolveTaggableInstructor(note, instructorId);
            notificationService.createNoteTaggedNotification(note, instructor, student);

            newTags.add(NoteTag.builder()
                    .note(note)
                    .taggedInstructor(instructor)
                    .taggedBy(student)
                    .notificationSent(true)
                    .build());
        }

        if (!newTags.isEmpty()) {
            noteTagRepository.saveAll(newTags);
            note.setLastTaggedAt(Instant.now());
        }
    }

    private User resolveTaggableInstructor(Note note, UUID instructorId) {
        User instructor = userRepository.findByIdAndDeletedAtIsNull(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found."));

        if (instructor.getRoleType() != RoleType.INSTRUCTOR || instructor.getStatus() != UserStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Only approved instructors can be tagged.");
        }

        if (!isInstructorTaggableForNote(note, instructor)) {
            if (note.getCourseSession() != null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Only the assigned session instructor can be tagged for this note.");
            }
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Only instructors assigned to this course can be tagged.");
        }

        return instructor;
    }

    private void removeInvalidTags(Note note) {
        List<NoteTag> invalidTags = noteTagRepository.findAllByNoteOrderByCreatedAtAsc(note).stream()
                .filter(noteTag -> !isInstructorTaggableForNote(note, noteTag.getTaggedInstructor()))
                .toList();

        if (!invalidTags.isEmpty()) {
            noteTagRepository.deleteAll(invalidTags);
        }
    }

    private boolean isInstructorTaggableForNote(Note note, User instructor) {
        if (note.getCourseSession() != null) {
            return note.getCourseSession().getInstructor().getId().equals(instructor.getId());
        }

        return courseSessionRepository.findAllByCourseOrderBySessionOrderAsc(note.getCourse()).stream()
                .anyMatch(session -> session.getInstructor().getId().equals(instructor.getId()));
    }

    private Set<UUID> normalizeInstructorIds(List<UUID> instructorIds) {
        if (instructorIds == null || instructorIds.isEmpty()) {
            return Set.of();
        }
        return new LinkedHashSet<>(instructorIds);
    }

    private void validateStudent(User user) {
        if (user.getRoleType() != RoleType.STUDENT) {
            throw new AccessDeniedBusinessException("Only students can manage notes.");
        }
    }
}
