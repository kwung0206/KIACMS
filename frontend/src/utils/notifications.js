const NOTIFICATIONS_UPDATED_EVENT = "kiacms:notifications-updated";

export function emitNotificationsUpdated() {
  window.dispatchEvent(new CustomEvent(NOTIFICATIONS_UPDATED_EVENT));
}

export function subscribeNotificationsUpdated(listener) {
  window.addEventListener(NOTIFICATIONS_UPDATED_EVENT, listener);
  return () => window.removeEventListener(NOTIFICATIONS_UPDATED_EVENT, listener);
}

export function openNotificationTarget(navigate, targetUrl) {
  if (!targetUrl) {
    navigate("/notifications");
    return;
  }

  try {
    const resolved = new URL(targetUrl, window.location.origin);

    if (resolved.origin !== window.location.origin) {
      window.open(resolved.toString(), "_blank", "noopener,noreferrer");
      return;
    }

    navigate(`${resolved.pathname}${resolved.search}${resolved.hash}`);
  } catch (error) {
    navigate(targetUrl);
  }
}
