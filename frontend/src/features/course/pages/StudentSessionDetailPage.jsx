import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { fetchStudentSessionDetail } from "../../../api/courseApi";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDate, formatTime } from "../../../utils/date";

function ResourceLinkCard({ title, href, description }) {
  return (
    <div className="info-card">
      <strong>{title}</strong>
      <span>{description}</span>
      {href ? (
        <a href={href} target="_blank" rel="noreferrer">
          링크 열기
        </a>
      ) : (
        <small className="muted-text">아직 등록되지 않았습니다.</small>
      )}
    </div>
  );
}

export default function StudentSessionDetailPage() {
  const { sessionId } = useParams();
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const response = await fetchStudentSessionDetail(sessionId);
        if (active) {
          setSession(response);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      active = false;
    };
  }, [sessionId]);

  if (loading) {
    return <LoadingScreen message="회차 상세를 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={session.title}
        description={`${session.courseTitle} · ${session.courseCode}`}
        actions={
          <Link className="ghost-button button-small" to="/student/calendar">
            캘린더로 돌아가기
          </Link>
        }
      />

      <section className="detail-grid">
        <article className="panel">
          <div className="spread-row">
            <h2>회차 정보</h2>
            <StatusBadge value={session.status} />
          </div>

          <dl className="detail-list">
            <div>
              <dt>회차</dt>
              <dd>{session.sessionOrder}회차</dd>
            </div>
            <div>
              <dt>과정</dt>
              <dd>{session.courseTitle}</dd>
            </div>
            <div>
              <dt>날짜</dt>
              <dd>{formatDate(session.sessionDate)}</dd>
            </div>
            <div>
              <dt>시간</dt>
              <dd>
                {formatTime(session.startTime)} - {formatTime(session.endTime)}
              </dd>
            </div>
            <div>
              <dt>강의실</dt>
              <dd>{session.classroom || "-"}</dd>
            </div>
            <div>
              <dt>강사</dt>
              <dd>{session.instructorName}</dd>
            </div>
          </dl>

          <p className="detail-description">
            {session.description || "등록된 회차 설명이 없습니다."}
          </p>
        </article>

        <article className="panel">
          <h2>학습 리소스</h2>
          <div className="list-stack">
            <ResourceLinkCard
              title="Zoom 링크"
              href={session.resource?.zoomLink}
              description="실시간 수업 입장 링크"
            />
            <ResourceLinkCard
              title="녹화본 링크"
              href={session.resource?.recordingLink}
              description="수업 종료 후 다시보기 링크"
            />
            <ResourceLinkCard
              title="정리 링크"
              href={session.resource?.summaryLink}
              description="강의 정리 및 추가 자료 링크"
            />
            <div className="info-card">
              <strong>추가 안내</strong>
              <p>{session.resource?.additionalNotice || "등록된 추가 안내가 없습니다."}</p>
            </div>
          </div>
        </article>
      </section>
    </div>
  );
}
