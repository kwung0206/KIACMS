import { buildQuery, request } from "./http";

export function fetchProjectBoard(status) {
  return request(`/api/projects${buildQuery({ status })}`);
}

export function fetchProjectDetail(postId) {
  return request(`/api/projects/${postId}`);
}

export function createProjectPost(payload) {
  return request("/api/student/projects", {
    method: "POST",
    body: payload,
  });
}

export function fetchMyProjectPosts() {
  return request("/api/student/projects/me");
}

export function fetchProjectManagement(postId) {
  return request(`/api/student/projects/${postId}/manage`);
}

export function decideProjectApplication(postId, applicationId, payload) {
  return request(`/api/student/projects/${postId}/applications/${applicationId}/decision`, {
    method: "PATCH",
    body: payload,
  });
}

export function decideMentorApplication(postId, applicationId, payload) {
  return request(`/api/student/projects/${postId}/mentor-applications/${applicationId}/decision`, {
    method: "PATCH",
    body: payload,
  });
}

export function submitProjectApplication(positionId, payload) {
  return request(`/api/student/project-applications/positions/${positionId}`, {
    method: "POST",
    body: payload,
  });
}

export function fetchMyProjectApplications() {
  return request("/api/student/project-applications/me");
}

export function fetchMyProjectApplication(applicationId) {
  return request(`/api/student/project-applications/${applicationId}`);
}

export function updateProjectApplication(applicationId, payload) {
  return request(`/api/student/project-applications/${applicationId}`, {
    method: "PUT",
    body: payload,
  });
}

export function deleteProjectApplication(applicationId) {
  return request(`/api/student/project-applications/${applicationId}`, {
    method: "DELETE",
  });
}

export function withdrawProjectApplication(applicationId) {
  return request(`/api/student/project-applications/${applicationId}/withdraw`, {
    method: "PATCH",
  });
}

export function deleteMyProjectPost(postId) {
  return request(`/api/student/projects/${postId}`, {
    method: "DELETE",
  });
}

export function submitMentorApplication(postId, payload) {
  return request(`/api/projects/${postId}/mentor-applications`, {
    method: "POST",
    body: payload,
  });
}

export function fetchMyMentorApplications() {
  return request("/api/project-mentor-applications/me");
}

export function withdrawMentorApplication(applicationId) {
  return request(`/api/project-mentor-applications/${applicationId}/withdraw`, {
    method: "PATCH",
  });
}
