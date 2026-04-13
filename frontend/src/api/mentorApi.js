import { request } from "./http";

export function searchMentorStudents(keyword) {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : "";
  return request(`/api/mentor/students${query}`);
}

export function fetchManagedStudents() {
  return request("/api/mentor/managed-students");
}

export function fetchMentorCourses() {
  return request("/api/mentor/courses");
}

export function assignManagedStudent(payload) {
  return request("/api/mentor/student-mappings", {
    method: "POST",
    body: payload,
  });
}

export function endManagedStudent(mappingId) {
  return request(`/api/mentor/student-mappings/${mappingId}/end`, {
    method: "PATCH",
  });
}

export function assignCourseToManagedStudent(mappingId, courseId) {
  return request(`/api/mentor/student-mappings/${mappingId}/course-enrollments`, {
    method: "POST",
    body: { courseId },
  });
}

export function dropCourseFromManagedStudent(mappingId, courseId) {
  return request(`/api/mentor/student-mappings/${mappingId}/course-enrollments/${courseId}`, {
    method: "DELETE",
  });
}
