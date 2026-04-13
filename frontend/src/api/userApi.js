import { request } from "./http";

export function fetchMyProfile() {
  return request("/api/users/me");
}

export function updateMyProfile(payload) {
  return request("/api/users/me", {
    method: "PUT",
    body: payload,
  });
}

export function changeMyPassword(payload) {
  return request("/api/users/me/password", {
    method: "PUT",
    body: payload,
  });
}

export function fetchRoleDashboard(role) {
  const routeByRole = {
    STUDENT: "/api/student/dashboard",
    INSTRUCTOR: "/api/instructor/dashboard",
    MENTOR: "/api/mentor/dashboard",
    ROOT: "/api/root/dashboard",
  };

  return request(routeByRole[role]);
}
