import { buildQuery, request } from "./http";

export function fetchStudentCalendar({ from, to }) {
  return request(`/api/student/calendar${buildQuery({ from, to })}`);
}

export function fetchStudentSessionDetail(sessionId) {
  return request(`/api/student/sessions/${sessionId}`);
}

export function fetchInstructorSessions() {
  return request("/api/instructor/sessions");
}

export function fetchInstructorSessionDetail(sessionId) {
  return request(`/api/instructor/sessions/${sessionId}`);
}

export function updateInstructorSessionResource(sessionId, payload) {
  return request(`/api/instructor/sessions/${sessionId}/resource`, {
    method: "PUT",
    body: payload,
  });
}
