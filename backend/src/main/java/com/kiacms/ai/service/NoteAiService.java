package com.kiacms.ai.service;

import com.kiacms.ai.dto.response.NoteAiSummaryResponse;
import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.ai.enums.AiReferenceType;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.note.entity.Note;
import com.kiacms.note.repository.NoteRepository;
import com.kiacms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteAiService {

    private final NoteRepository noteRepository;
    private final OpenAiStructuredResponseClient openAiStructuredResponseClient;
    private final AiPromptFactory aiPromptFactory;
    private final AiSchemaFactory aiSchemaFactory;

    @Transactional(readOnly = true)
    public NoteAiSummaryResponse summarizeNote(java.util.UUID noteId, User student) {
        Note note = noteRepository.findByIdAndAuthor(noteId, student)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found."));

        return openAiStructuredResponseClient.requestStructuredOutput(
                student,
                AiFeatureType.NOTE_SUMMARY,
                AiReferenceType.NOTE,
                note.getId(),
                aiPromptFactory.buildNoteSummarySystemPrompt(),
                aiPromptFactory.buildNoteSummaryUserPrompt(note),
                "note_summary_response",
                aiSchemaFactory.noteSummarySchema(),
                NoteAiSummaryResponse.class,
                800
        );
    }
}
