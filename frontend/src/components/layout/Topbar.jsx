import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  deleteAllNotifications,
  deleteNotification,
  fetchNotifications,
  fetchUnreadNotificationCount,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from "../../api/notificationApi";
import ChatbotDrawer from "../chatbot/ChatbotDrawer";
import ConfirmModal from "../common/ConfirmModal";
import { useAuth } from "../../hooks/useAuth";
import { formatDateTime } from "../../utils/date";
import {
  emitNotificationsUpdated,
  openNotificationTarget,
  subscribeNotificationsUpdated,
} from "../../utils/notifications";
import { getRoleLabel } from "../../utils/userLabels";

function getTitleFromPath(pathname) {
  if (pathname.startsWith("/student/calendar")) return "학생 수업 캘린더";
  if (pathname.startsWith("/student/sessions/")) return "수업 상세";
  if (pathname.startsWith("/student/notes/") && pathname.endsWith("/edit")) return "정리글 수정";
  if (pathname.startsWith("/student/notes/new")) return "정리글 작성";
  if (pathname.startsWith("/student/notes/")) return "정리글 상세";
  if (pathname.startsWith("/student/notes")) return "내 정리글";
  if (pathname.startsWith("/student/applications")) return "내 지원 현황";
  if (pathname.startsWith("/student/projects/new")) return "새 모집글 작성";
  if (pathname.startsWith("/student/projects/") && pathname.endsWith("/manage")) return "지원서 관리";
  if (pathname.startsWith("/student/projects")) return "내 모집글";
  if (pathname.startsWith("/instructor/sessions")) return "담당 회차 관리";
  if (pathname.startsWith("/instructor/tagged-notes")) return "태그된 정리글";
  if (pathname.startsWith("/instructor/project-mentoring")) return "프로젝트 지원 이력";
  if (pathname.startsWith("/mentor/students")) return "관리 학생";
  if (pathname.startsWith("/mentor")) return "멘토 홈";
  if (pathname.startsWith("/root/courses")) return "수업 일정 관리";
  if (pathname.startsWith("/root/projects")) return "프로젝트 삭제 관리";
  if (pathname.startsWith("/root")) return "Root 운영 홈";
  if (pathname.startsWith("/projects/")) return "프로젝트 상세";
  if (pathname.startsWith("/projects")) return "프로젝트 게시판";
  if (pathname.startsWith("/notifications")) return "알림";
  if (pathname.startsWith("/me")) return "마이페이지";
  return "KIACMS";
}

function ChatbotIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="chatbot-button-icon">
      <path
        d="M5 6.75A2.75 2.75 0 0 1 7.75 4h8.5A2.75 2.75 0 0 1 19 6.75v5.5A2.75 2.75 0 0 1 16.25 15H11.5l-3.72 3.1c-.49.41-1.28.06-1.28-.58V15.3A2.75 2.75 0 0 1 5 12.25Z"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M9 9.25h6M9 11.75h4"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  );
}

export default function Topbar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const [chatbotOpen, setChatbotOpen] = useState(false);
  const [logoutModalOpen, setLogoutModalOpen] = useState(false);
  const [deleteAllModalOpen, setDeleteAllModalOpen] = useState(false);
  const [dropdownError, setDropdownError] = useState("");

  const showNotifications = user?.roleType !== "MENTOR";
  const title = useMemo(() => getTitleFromPath(location.pathname), [location.pathname]);
  const identityLabel = useMemo(() => {
    const roleLabel = getRoleLabel(user?.roleType);
    return user?.name ? `${roleLabel} ${user.name}` : roleLabel;
  }, [user?.name, user?.roleType]);

  useEffect(() => {
    if (!showNotifications) {
      setNotifications([]);
      setUnreadCount(0);
      return undefined;
    }

    let active = true;

    async function loadNotifications() {
      try {
        const [list, unread] = await Promise.all([
          fetchNotifications({ unreadOnly: false }),
          fetchUnreadNotificationCount(),
        ]);

        if (!active) {
          return;
        }

        setNotifications(list.slice(0, 5));
        setUnreadCount(unread.unreadCount);
        setDropdownError("");
      } catch (error) {
        if (!active) {
          return;
        }

        setNotifications([]);
        setUnreadCount(0);
        setDropdownError(error.message);
      }
    }

    loadNotifications();
    const unsubscribe = subscribeNotificationsUpdated(loadNotifications);

    return () => {
      active = false;
      unsubscribe();
    };
  }, [location.pathname, showNotifications]);

  useEffect(() => {
    setIsOpen(false);
  }, [location.pathname]);

  async function handleNotificationClick(notification) {
    try {
      if (!notification.isRead) {
        await markNotificationAsRead(notification.id);
        setUnreadCount((current) => Math.max(current - 1, 0));
        setNotifications((current) =>
          current.map((item) => (item.id === notification.id ? { ...item, isRead: true } : item)),
        );
        emitNotificationsUpdated();
      }

      setIsOpen(false);
      openNotificationTarget(navigate, notification.targetUrl);
    } catch (error) {
      setDropdownError(error.message);
    }
  }

  async function handleMarkNotificationAsRead(notification) {
    try {
      await markNotificationAsRead(notification.id);
      setUnreadCount((current) => Math.max(current - 1, 0));
      setNotifications((current) =>
        current.map((item) => (item.id === notification.id ? { ...item, isRead: true } : item)),
      );
      emitNotificationsUpdated();
    } catch (error) {
      setDropdownError(error.message);
    }
  }

  async function handleDeleteNotification(notification) {
    try {
      await deleteNotification(notification.id);
      setNotifications((current) => current.filter((item) => item.id !== notification.id));
      if (!notification.isRead) {
        setUnreadCount((current) => Math.max(current - 1, 0));
      }
      emitNotificationsUpdated();
    } catch (error) {
      setDropdownError(error.message);
    }
  }

  async function handleReadAll() {
    try {
      await markAllNotificationsAsRead();
      setUnreadCount(0);
      setNotifications((current) => current.map((item) => ({ ...item, isRead: true })));
      emitNotificationsUpdated();
    } catch (error) {
      setDropdownError(error.message);
    }
  }

  async function handleDeleteAllNotifications() {
    try {
      await deleteAllNotifications();
      setUnreadCount(0);
      setNotifications([]);
      setDeleteAllModalOpen(false);
      emitNotificationsUpdated();
    } catch (error) {
      setDropdownError(error.message);
    }
  }

  function handleLogoutConfirm() {
    logout();
    setLogoutModalOpen(false);
    navigate("/login", { replace: true });
  }

  return (
    <>
      <header className="topbar">
        <div className="topbar-title-group">
          <h1>{title}</h1>
        </div>

        <div className="topbar-actions">
          <button
            className="ghost-button button-small chatbot-toggle-button"
            type="button"
            onClick={() => setChatbotOpen(true)}
          >
            <ChatbotIcon />
            <span>AI 도우미</span>
          </button>

          {showNotifications ? (
            <div className="notification-wrapper">
              <button
                className="ghost-button button-small notification-button"
                type="button"
                onClick={() => setIsOpen((current) => !current)}
              >
                알림
                {unreadCount > 0 ? <span className="notification-count">{unreadCount}</span> : null}
              </button>

              {isOpen ? (
                <div className="notification-dropdown">
                  <div className="notification-dropdown-header">
                    <strong>최근 알림</strong>
                    <div className="inline-actions compact-inline-actions">
                      <button type="button" className="text-button" onClick={handleReadAll}>
                        전체 읽음
                      </button>
                      <button
                        type="button"
                        className="text-button danger-text-button"
                        onClick={() => setDeleteAllModalOpen(true)}
                      >
                        전체 삭제
                      </button>
                    </div>
                  </div>

                  {dropdownError ? <div className="form-alert error">{dropdownError}</div> : null}

                  {notifications.length === 0 ? (
                    <p className="muted-text">표시할 알림이 없습니다.</p>
                  ) : (
                    <div className="list-stack compact-list">
                      {notifications.map((notification) => (
                        <article
                          key={notification.id}
                          className={`notification-item ${
                            notification.isRead ? "notification-read" : "notification-unread"
                          }`}
                        >
                          <button
                            type="button"
                            className="notification-item-content"
                            onClick={() => handleNotificationClick(notification)}
                          >
                            <strong>{notification.title}</strong>
                            <span>{notification.message}</span>
                            <small>{formatDateTime(notification.createdAt)}</small>
                          </button>

                          <div className="notification-item-actions">
                            {!notification.isRead ? (
                              <button
                                type="button"
                                className="text-button"
                                onClick={() => handleMarkNotificationAsRead(notification)}
                              >
                                읽음
                              </button>
                            ) : null}
                            <button
                              type="button"
                              className="text-button danger-text-button"
                              onClick={() => handleDeleteNotification(notification)}
                            >
                              삭제
                            </button>
                          </div>
                        </article>
                      ))}
                    </div>
                  )}

                  <button
                    type="button"
                    className="text-button full-width"
                    onClick={() => navigate("/notifications")}
                  >
                    알림 페이지로 이동
                  </button>
                </div>
              ) : null}
            </div>
          ) : null}

          <div className="topbar-user">
            <strong>{identityLabel}</strong>
          </div>

          <button
            className="primary-button subtle button-small"
            type="button"
            onClick={() => setLogoutModalOpen(true)}
          >
            로그아웃
          </button>
        </div>
      </header>

      <ChatbotDrawer
        open={chatbotOpen}
        currentPath={location.pathname}
        onClose={() => setChatbotOpen(false)}
      />

      <ConfirmModal
        open={logoutModalOpen}
        title="로그아웃"
        description="로그아웃 하시겠습니까?"
        confirmLabel="로그아웃"
        cancelLabel="취소"
        tone="danger"
        onConfirm={handleLogoutConfirm}
        onCancel={() => setLogoutModalOpen(false)}
      />

      <ConfirmModal
        open={deleteAllModalOpen}
        title="알림 전체 삭제"
        description="모든 알림을 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다."
        confirmLabel="전체 삭제"
        cancelLabel="취소"
        tone="danger"
        onConfirm={handleDeleteAllNotifications}
        onCancel={() => setDeleteAllModalOpen(false)}
      />
    </>
  );
}
