import { buildQuery, request } from "./http";

export function fetchPendingApprovals() {
  return request("/api/admin/approvals/users/pending");
}

export function approveUser(userId) {
  return request(`/api/admin/approvals/users/${userId}/approve`, {
    method: "POST",
  });
}

export function rejectUser(userId, rejectionReason) {
  return request(`/api/admin/approvals/users/${userId}/reject`, {
    method: "POST",
    body: { reason: rejectionReason },
  });
}

export function fetchRootCourses() {
  return request("/api/root/courses");
}

export function fetchRootInstructors() {
  return request("/api/root/courses/instructors");
}

export function fetchRootSessionCalendar(range) {
  return request(`/api/root/courses/sessions/calendar${buildQuery(range)}`);
}

export function createCourse(payload) {
  return request("/api/root/courses", {
    method: "POST",
    body: payload,
  });
}

export function createCourseSession(courseId, payload) {
  return request(`/api/root/courses/${courseId}/sessions`, {
    method: "POST",
    body: payload,
  });
}

export function updateCourseSession(sessionId, payload) {
  return request(`/api/root/courses/sessions/${sessionId}`, {
    method: "PUT",
    body: payload,
  });
}

export function deleteCourseSession(sessionId) {
  return request(`/api/root/courses/sessions/${sessionId}`, {
    method: "DELETE",
  });
}
