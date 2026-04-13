import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  deleteAllNotifications,
  deleteNotification,
  fetchNotifications,
  fetchUnreadNotificationCount,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from "../../../api/notificationApi";
import ConfirmModal from "../../../components/common/ConfirmModal";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDateTime } from "../../../utils/date";
import {
  emitNotificationsUpdated,
  openNotificationTarget,
} from "../../../utils/notifications";

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [deleteAllOpen, setDeleteAllOpen] = useState(false);

  useEffect(() => {
    loadNotifications(unreadOnly);
  }, []);

  async function loadNotifications(nextUnreadOnly = unreadOnly) {
    setLoading(true);
    setError("");

    try {
      const [list, unread] = await Promise.all([
        fetchNotifications({ unreadOnly: nextUnreadOnly }),
        fetchUnreadNotificationCount(),
      ]);

      setNotifications(list);
      setUnreadCount(unread.unreadCount);
    } catch (loadError) {
      setNotifications([]);
      setUnreadCount(0);
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleUnreadOnlyChange(event) {
    const nextUnreadOnly = event.target.checked;
    setUnreadOnly(nextUnreadOnly);
    await loadNotifications(nextUnreadOnly);
  }

  async function handleMarkAsRead(notification) {
    try {
      const updated = await markNotificationAsRead(notification.id);

      setNotifications((current) =>
        unreadOnly
          ? current.filter((item) => item.id !== notification.id)
          : current.map((item) => (item.id === notification.id ? updated : item)),
      );

      setUnreadCount((current) => Math.max(current - 1, 0));
      setFeedback({ type: "success", message: "알림을 읽음 처리했습니다." });
      emitNotificationsUpdated();
    } catch (markError) {
      setFeedback({ type: "error", message: markError.message });
    }
  }

  async function handleDelete(notification) {
    try {
      await deleteNotification(notification.id);
      setNotifications((current) => current.filter((item) => item.id !== notification.id));
      if (!notification.isRead) {
        setUnreadCount((current) => Math.max(current - 1, 0));
      }
      setFeedback({ type: "success", message: "알림이 삭제되었습니다." });
      emitNotificationsUpdated();
    } catch (deleteError) {
      setFeedback({ type: "error", message: deleteError.message });
    }
  }

  async function handleOpenNotification(notification) {
    if (!notification.isRead) {
      await handleMarkAsRead(notification);
    }

    openNotificationTarget(navigate, notification.targetUrl);
  }

  async function handleMarkAllAsRead() {
    try {
      await markAllNotificationsAsRead();
      setUnreadCount(0);
      setNotifications((current) =>
        unreadOnly ? [] : current.map((item) => ({ ...item, isRead: true })),
      );
      setFeedback({ type: "success", message: "모든 알림을 읽음 처리했습니다." });
      emitNotificationsUpdated();
    } catch (markAllError) {
      setFeedback({ type: "error", message: markAllError.message });
    }
  }

  async function handleDeleteAll() {
    try {
      await deleteAllNotifications();
      setNotifications([]);
      setUnreadCount(0);
      setDeleteAllOpen(false);
      setFeedback({ type: "success", message: "모든 알림이 삭제되었습니다." });
      emitNotificationsUpdated();
    } catch (deleteError) {
      setFeedback({ type: "error", message: deleteError.message });
    }
  }

  return (
    <>
      <div className="page-stack">
        <PageHeader
          title="알림"
          description="승인 결과, 강사 태그, 프로젝트 결과 등 주요 이벤트를 한곳에서 읽고 삭제할 수 있습니다."
          actions={
            <div className="page-action-group">
              <button className="ghost-button button-small" type="button" onClick={handleMarkAllAsRead}>
                전체 읽음 처리
              </button>
              <button
                className="danger-button button-small"
                type="button"
                onClick={() => setDeleteAllOpen(true)}
              >
                전체 삭제
              </button>
            </div>
          }
        />

        <div className="panel">
          <div className="summary-card">
            <div>
              <span>읽지 않은 알림</span>
              <strong>{unreadCount}</strong>
            </div>
            <div>
              <span>현재 목록</span>
              <strong>{notifications.length}</strong>
            </div>
          </div>

          <label className="checkbox-row spaced-top">
            <input type="checkbox" checked={unreadOnly} onChange={handleUnreadOnlyChange} />
            읽지 않은 알림만 보기
          </label>
        </div>

        {error ? <div className="form-alert error">{error}</div> : null}
        {feedback.message ? (
          <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
            {feedback.message}
          </div>
        ) : null}

        {loading ? (
          <LoadingScreen message="알림 목록을 불러오는 중입니다." />
        ) : notifications.length === 0 ? (
          <EmptyState
            title="표시할 알림이 없습니다."
            description="새로운 승인 결과나 프로젝트 이벤트가 생기면 이곳에 표시됩니다."
          />
        ) : (
          <div className="list-stack">
            {notifications.map((notification) => (
              <article key={notification.id} className="panel info-card">
                <div className="spread-row">
                  <div className="list-stack compact-list">
                    <strong>{notification.title}</strong>
                    <span>{notification.message}</span>
                  </div>
                  <StatusBadge value={notification.isRead ? "READ" : "UNREAD"} />
                </div>

                <div className="project-meta tight">
                  <small>유형: {notification.type}</small>
                  <small>대상: {notification.targetType || "-"}</small>
                  <small>발생 시각: {formatDateTime(notification.createdAt)}</small>
                  <small>읽음 시각: {formatDateTime(notification.readAt)}</small>
                </div>

                <div className="inline-actions">
                  <button
                    className="ghost-button button-small"
                    type="button"
                    onClick={() => handleOpenNotification(notification)}
                  >
                    관련 화면 열기
                  </button>
                  {!notification.isRead ? (
                    <button
                      className="primary-button subtle button-small"
                      type="button"
                      onClick={() => handleMarkAsRead(notification)}
                    >
                      읽음 처리
                    </button>
                  ) : null}
                  <button
                    className="danger-button button-small"
                    type="button"
                    onClick={() => handleDelete(notification)}
                  >
                    삭제
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>

      <ConfirmModal
        open={deleteAllOpen}
        title="알림 전체 삭제"
        description="모든 알림을 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다."
        confirmLabel="전체 삭제"
        cancelLabel="취소"
        tone="danger"
        onConfirm={handleDeleteAll}
        onCancel={() => setDeleteAllOpen(false)}
      />
    </>
  );
}
