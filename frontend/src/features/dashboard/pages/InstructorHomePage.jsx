import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchInstructorSessions } from "../../../api/courseApi";
import { fetchTaggedNotes } from "../../../api/noteApi";
import {
  fetchNotifications,
  fetchUnreadNotificationCount,
} from "../../../api/notificationApi";
import { fetchRoleDashboard } from "../../../api/userApi";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import { useAuth } from "../../../hooks/useAuth";
import { formatDate, formatDateTime, formatTime } from "../../../utils/date";

export default function InstructorHomePage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [taggedNotes, setTaggedNotes] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function load() {
      const results = await Promise.allSettled([
        fetchRoleDashboard("INSTRUCTOR"),
        fetchInstructorSessions(),
        fetchTaggedNotes(),
        fetchNotifications({ unreadOnly: true }),
        fetchUnreadNotificationCount(),
      ]);

      if (!active) {
        return;
      }

      const [dashboardResult, sessionResult, noteResult, notificationResult, unreadResult] =
        results;

      if (dashboardResult.status === "fulfilled") {
        setDashboard(dashboardResult.value);
      }
      if (sessionResult.status === "fulfilled") {
        setSessions(sessionResult.value.slice(0, 5));
      }
      if (noteResult.status === "fulfilled") {
        setTaggedNotes(noteResult.value.slice(0, 5));
      }
      if (notificationResult.status === "fulfilled") {
        setNotifications(notificationResult.value.slice(0, 4));
      }
      if (unreadResult.status === "fulfilled") {
        setUnreadCount(unreadResult.value.unreadCount);
      }

      setLoading(false);
    }

    load();
    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return <LoadingScreen message="강사 홈을 불러오는 중..." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={`${user?.name || "강사"}님, 오늘 담당 수업을 확인해 주세요.`}
        description="담당 회차 리소스 관리, 태그된 정리글 피드백, 알림 확인을 빠르게 처리할 수 있습니다."
        actions={
          <div className="inline-actions">
            <Link className="primary-button" to="/instructor/sessions">
              회차 관리
            </Link>
            <Link className="ghost-button" to="/instructor/tagged-notes">
              태그된 정리글
            </Link>
          </div>
        }
      />

      <section className="stats-grid">
        <article className="panel stat-card">
          <span>계정 요약</span>
          <strong>{dashboard?.role || user?.roleType}</strong>
          <p>{dashboard?.email || user?.email}</p>
        </article>
        <article className="panel stat-card">
          <span>담당 회차</span>
          <strong>{sessions.length}</strong>
          <p>현재 확인 가능한 담당 회차 수입니다.</p>
        </article>
        <article className="panel stat-card">
          <span>읽지 않은 알림</span>
          <strong>{unreadCount}</strong>
          <p>학생 태그, 코멘트, 리소스 관련 알림을 확인해 주세요.</p>
        </article>
      </section>

      <section className="grid-two">
        <article className="panel">
          <div className="section-title-row">
            <h2>담당 회차 요약</h2>
            <Link className="text-button" to="/instructor/sessions">
              전체 보기
            </Link>
          </div>

          {sessions.length === 0 ? (
            <EmptyState
              title="배정된 회차가 없습니다."
              description="root가 회차를 배정하면 이곳에서 수업 리소스를 관리할 수 있습니다."
            />
          ) : (
            <div className="list-stack">
              {sessions.map((session) => (
                <div key={session.id} className="info-card">
                  <strong>{session.courseTitle}</strong>
                  <span>{session.title}</span>
                  <small>
                    {formatDate(session.sessionDate)} {formatTime(session.startTime)} -{" "}
                    {formatTime(session.endTime)}
                  </small>
                  <small>Zoom: {session.resource?.zoomLink || "미등록"}</small>
                </div>
              ))}
            </div>
          )}
        </article>

        <article className="panel">
          <div className="section-title-row">
            <h2>태그된 정리글</h2>
            <Link className="text-button" to="/instructor/tagged-notes">
              목록 보기
            </Link>
          </div>

          {taggedNotes.length === 0 ? (
            <EmptyState
              title="태그된 정리글이 없습니다."
              description="학생이 강사 태그를 남기면 이곳에서 바로 확인할 수 있습니다."
            />
          ) : (
            <div className="list-stack">
              {taggedNotes.map((note) => (
                <Link
                  key={note.noteId}
                  className="card-link"
                  to={`/instructor/tagged-notes/${note.noteId}`}
                >
                  <strong>{note.noteTitle}</strong>
                  <span>{note.authorName}</span>
                  <small>{note.courseTitle}</small>
                </Link>
              ))}
            </div>
          )}
        </article>
      </section>

      <article className="panel">
        <div className="section-title-row">
          <h2>알림 요약</h2>
          <Link className="text-button" to="/notifications">
            알림 페이지
          </Link>
        </div>

        {notifications.length === 0 ? (
          <p className="muted-text">새 알림이 없습니다.</p>
        ) : (
          <div className="list-stack">
            {notifications.map((notification) => (
              <div key={notification.id} className="info-card">
                <strong>{notification.title}</strong>
                <span>{notification.message}</span>
                <small>{formatDateTime(notification.createdAt)}</small>
              </div>
            ))}
          </div>
        )}
      </article>
    </div>
  );
}
