import { request } from "./http";

export function fetchRootProjectPosts() {
  return request("/api/root/projects");
}

export function deleteRootProject(postId, reason) {
  return request(`/api/root/projects/${postId}`, {
    method: "DELETE",
    body: { reason },
  });
}

export function fetchRootProjectDeletionHistory() {
  return request("/api/root/projects/deletions");
}
