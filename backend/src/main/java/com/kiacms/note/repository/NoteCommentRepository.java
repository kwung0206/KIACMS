package com.kiacms.note.repository;

import com.kiacms.note.entity.Note;
import com.kiacms.note.entity.NoteComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteCommentRepository extends JpaRepository<NoteComment, UUID> {

    List<NoteComment> findAllByNoteOrderByCreatedAtAsc(Note note);

    long countByNote(Note note);

    void deleteAllByNote(Note note);
}
