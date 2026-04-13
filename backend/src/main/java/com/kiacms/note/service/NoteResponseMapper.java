package com.kiacms.note.service;

import com.kiacms.note.dto.response.NoteDetailResponse;
import com.kiacms.note.dto.response.NoteSummaryResponse;
import com.kiacms.note.dto.response.TaggedNoteSummaryResponse;
import com.kiacms.note.entity.Note;
import com.kiacms.note.entity.NoteTag;
import com.kiacms.note.repository.NoteCommentRepository;
import com.kiacms.note.repository.NoteTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteResponseMapper {

    private final NoteTagRepository noteTagRepository;
    private final NoteCommentRepository noteCommentRepository;

    public NoteSummaryResponse toSummary(Note note) {
        return NoteSummaryResponse.from(
                note,
                noteTagRepository.countByNote(note),
                noteCommentRepository.countByNote(note)
        );
    }

    public TaggedNoteSummaryResponse toTaggedSummary(NoteTag noteTag) {
        return TaggedNoteSummaryResponse.from(
                noteTag,
                noteCommentRepository.countByNote(noteTag.getNote())
        );
    }

    public NoteDetailResponse toDetail(Note note) {
        return NoteDetailResponse.from(
                note,
                noteTagRepository.findAllByNoteOrderByCreatedAtAsc(note),
                noteCommentRepository.findAllByNoteOrderByCreatedAtAsc(note)
        );
    }
}
