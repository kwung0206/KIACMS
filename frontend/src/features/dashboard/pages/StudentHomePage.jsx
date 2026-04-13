import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchStudentCalendar } from "../../../api/courseApi";
import { fetchMyNotes } from "../../../api/noteApi";
import {
  fetchNotifications,
  fetchUnreadNotificationCount,
} from "../../../api/notificationApi";
import { fetchProjectBoard } from "../../../api/projectApi";
import { fetchRoleDashboard } from "../../../api/userApi";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import { useAuth } from "../../../hooks/useAuth";
import { formatDateTime, todayRange } from "../../../utils/date";

export default function StudentHomePage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [notes, setNotes] = useState([]);
  const [projectPosts, setProjectPosts] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function load() {
      const range = todayRange();
      const results = await Promise.allSettled([
        fetchRoleDashboard("STUDENT"),
        fetchStudentCalendar(range),
        fetchMyNotes(),
        fetchProjectBoard("OPEN"),
        fetchNotifications({ unreadOnly: true }),
        fetchUnreadNotificationCount(),
      ]);

      if (!active) {
        return;
      }

      const [dashboardResult, calendarResult, noteResult, projectResult, notificationResult, unreadResult] =
        results;

      if (dashboardResult.status === "fulfilled") {
        setDashboard(dashboardResult.value);
      }
      if (calendarResult.status === "fulfilled") {
        setUpcomingEvents(calendarResult.value.events.slice(0, 4));
      }
      if (noteResult.status === "fulfilled") {
        setNotes(noteResult.value.slice(0, 4));
      }
      if (projectResult.status === "fulfilled") {
        setProjectPosts(projectResult.value.slice(0, 4));
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
    return <LoadingScreen message="학생 홈 화면을 불러오는 중..." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={`${user?.name || "학생"}님, 오늘도 학습을 이어가 볼까요?`}
        description="캘린더, 정리글, 프로젝트, 알림 상태를 한 화면에서 빠르게 확인할 수 있습니다."
        actions={
          <div className="inline-actions">
            <Link className="primary-button" to="/student/calendar">
              캘린더 보기
            </Link>
            <Link className="ghost-button" to="/student/notes/new">
              정리글 작성
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
          <span>다가오는 회차</span>
          <strong>{upcomingEvents.length}</strong>
          <p>현재 조회 범위 기준 예정된 수업 수입니다.</p>
        </article>
        <article className="panel stat-card">
          <span>읽지 않은 알림</span>
          <strong>{unreadCount}</strong>
          <p>강사 피드백, 프로젝트 결과, 운영 알림을 확인해 주세요.</p>
        </article>
      </section>

      <section className="grid-two">
        <article className="panel">
          <div className="section-title-row">
            <h2>빠른 이동</h2>
          </div>
          <div className="card-grid">
            <Link className="card-link" to="/student/calendar">
              <strong>수업 캘린더</strong>
              <span>수업 일정과 Zoom/녹화본 링크 확인</span>
            </Link>
            <Link className="card-link" to="/student/notes">
              <strong>내 정리글</strong>
              <span>정리글 조회, 수정, AI 요약 연결</span>
            </Link>
            <Link className="card-link" to="/projects">
              <strong>프로젝트 게시판</strong>
              <span>모집글 탐색 및 지원</span>
            </Link>
            <Link className="card-link" to="/notifications">
              <strong>알림 센터</strong>
              <span>읽지 않은 알림 {unreadCount}건 확인</span>
            </Link>
          </div>
        </article>

        <article className="panel">
          <div className="section-title-row">
            <h2>다가오는 수업</h2>
            <Link className="text-button" to="/student/calendar">
              전체 보기
            </Link>
          </div>

          {upcomingEvents.length === 0 ? (
            <EmptyState
              title="예정된 수업이 없습니다."
              description="캘린더에서 조회 범위를 조정하거나 등록된 수업을 확인해 주세요."
            />
          ) : (
            <div className="list-stack">
              {upcomingEvents.map((event) => (
                <Link key={event.id} className="card-link" to={`/student/sessions/${event.id}`}>
                  <strong>{event.title}</strong>
                  <span>{formatDateTime(event.start)}</span>
                  <small>{event.instructorName}</small>
                </Link>
              ))}
            </div>
          )}
        </article>
      </section>

      <section className="grid-two">
        <article className="panel">
          <div className="section-title-row">
            <h2>최근 정리글</h2>
            <Link className="text-button" to="/student/notes">
              목록 보기
            </Link>
          </div>

          {notes.length === 0 ? (
            <EmptyState
              title="정리글이 없습니다."
              description="첫 번째 정리글을 작성하고 강사 태그나 AI 요약을 활용해 보세요."
              action={
                <Link className="primary-button" to="/student/notes/new">
                  정리글 작성
                </Link>
              }
            />
          ) : (
            <div className="list-stack">
              {notes.map((note) => (
                <Link key={note.id} className="card-link" to={`/student/notes/${note.id}`}>
                  <strong>{note.title}</strong>
                  <span>{note.courseTitle}</span>
                  <small>
                    태그 {note.tagCount}건 · 코멘트 {note.commentCount}건
                  </small>
                </Link>
              ))}
            </div>
          )}
        </article>

        <article className="panel">
          <div className="section-title-row">
            <h2>열린 프로젝트 모집</h2>
            <Link className="text-button" to="/projects">
              게시판 보기
            </Link>
          </div>

          {projectPosts.length === 0 ? (
            <EmptyState
              title="열린 모집글이 없습니다."
              description="조금 뒤 다시 확인하거나 직접 모집글을 등록해 볼 수 있습니다."
            />
          ) : (
            <div className="list-stack">
              {projectPosts.map((post) => (
                <Link key={post.id} className="card-link" to={`/projects/${post.id}`}>
                  <strong>{post.title}</strong>
                  <span>{post.ownerName}</span>
                  <small>{post.techStack}</small>
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
