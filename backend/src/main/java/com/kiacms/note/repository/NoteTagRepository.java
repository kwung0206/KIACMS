package com.kiacms.note.repository;

import com.kiacms.note.entity.Note;
import com.kiacms.note.entity.NoteTag;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteTagRepository extends JpaRepository<NoteTag, UUID> {

    List<NoteTag> findAllByTaggedInstructorOrderByCreatedAtDesc(User taggedInstructor);

    List<NoteTag> findAllByNoteOrderByCreatedAtAsc(Note note);

    Optional<NoteTag> findByNoteAndTaggedInstructor(Note note, User taggedInstructor);

    long countByNote(Note note);

    void deleteAllByNote(Note note);
}
