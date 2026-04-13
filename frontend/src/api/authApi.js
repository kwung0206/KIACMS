import { request } from "./http";

export function login(payload) {
  return request("/api/auth/login", {
    method: "POST",
    auth: false,
    body: payload,
  });
}

export function signup(payload) {
  return request("/api/auth/signup", {
    method: "POST",
    auth: false,
    body: payload,
  });
}
