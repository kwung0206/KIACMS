import { buildQuery, request } from "./http";

export function fetchNotifications({ unreadOnly = false } = {}) {
  return request(`/api/notifications${buildQuery({ unreadOnly })}`);
}

export function fetchUnreadNotificationCount() {
  return request("/api/notifications/unread-count");
}

export function markNotificationAsRead(notificationId) {
  return request(`/api/notifications/${notificationId}/read`, {
    method: "PATCH",
  });
}

export function markAllNotificationsAsRead() {
  return request("/api/notifications/read-all", {
    method: "PATCH",
  });
}

export function deleteNotification(notificationId) {
  return request(`/api/notifications/${notificationId}`, {
    method: "DELETE",
  });
}

export function deleteAllNotifications() {
  return request("/api/notifications", {
    method: "DELETE",
  });
}
