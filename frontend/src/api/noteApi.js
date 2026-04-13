import { buildQuery, request } from "./http";

export function fetchMyNotes(filters = {}) {
  return request(`/api/student/notes${buildQuery(filters)}`);
}

export function fetchMyNoteDetail(noteId) {
  return request(`/api/student/notes/${noteId}`);
}

export function createNote(payload) {
  return request("/api/student/notes", {
    method: "POST",
    body: payload,
  });
}

export function updateNote(noteId, payload) {
  return request(`/api/student/notes/${noteId}`, {
    method: "PUT",
    body: payload,
  });
}

export function tagNoteInstructors(noteId, instructorIds) {
  return request(`/api/student/notes/${noteId}/tags`, {
    method: "POST",
    body: { instructorIds },
  });
}

export function deleteNote(noteId) {
  return request(`/api/student/notes/${noteId}`, {
    method: "DELETE",
  });
}

export function fetchTaggedNotes() {
  return request("/api/instructor/tagged-notes");
}

export function fetchTaggedNoteDetail(noteId) {
  return request(`/api/instructor/tagged-notes/${noteId}`);
}

export function createTaggedNoteComment(noteId, content) {
  return request(`/api/instructor/tagged-notes/${noteId}/comments`, {
    method: "POST",
    body: { content },
  });
}
