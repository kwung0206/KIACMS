import { request } from "./http";

export function summarizeNote(noteId) {
  return request(`/api/student/ai/notes/${noteId}/summary`, {
    method: "POST",
  });
}

export function requestCareerCourseRecommendations(payload) {
  return request("/api/ai/career-course-recommendations", {
    method: "POST",
    body: payload,
  });
}

export function requestSimilarProjectRecommendations(payload) {
  return request("/api/ai/similar-project-recommendations", {
    method: "POST",
    body: payload,
  });
}

export function sendChatbotMessage(payload) {
  return request("/api/ai/chatbot/messages", {
    method: "POST",
    body: payload,
  });
}

export function requestProjectPlanInsights(payload) {
  return request("/api/ai/project-plan-insights", {
    method: "POST",
    body: payload,
  });
}
