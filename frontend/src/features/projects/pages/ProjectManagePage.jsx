import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  decideMentorApplication,
  decideProjectApplication,
  fetchProjectManagement,
} from "../../../api/projectApi";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDate, formatDateTime } from "../../../utils/date";

function reasonKey(type, applicationId) {
  return `${type}:${applicationId}`;
}

export default function ProjectManagePage() {
  const { postId } = useParams();
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [reasons, setReasons] = useState({});
  const [savingKey, setSavingKey] = useState("");

  useEffect(() => {
    loadOverview();
  }, [postId]);

  async function loadOverview() {
    setLoading(true);
    setError("");

    try {
      const response = await fetchProjectManagement(postId);
      setOverview(response);
    } catch (loadError) {
      setOverview(null);
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }

  function getReason(type, applicationId) {
    return reasons[reasonKey(type, applicationId)] || "";
  }

  function setReason(type, applicationId, value) {
    setReasons((current) => ({
      ...current,
      [reasonKey(type, applicationId)]: value,
    }));
  }

  async function handleDecision(type, applicationId, status) {
    const typedReasonKey = reasonKey(type, applicationId);
    const rejectionReason = status === "REJECTED" ? (reasons[typedReasonKey] || "").trim() : "";

    setFeedback({ type: "", message: "" });

    if (status === "REJECTED" && !rejectionReason) {
      setFeedback({ type: "error", message: "반려 사유를 입력해 주세요." });
      return;
    }

    setSavingKey(typedReasonKey);

    try {
      if (type === "project") {
        await decideProjectApplication(postId, applicationId, { status, rejectionReason });
      } else {
        await decideMentorApplication(postId, applicationId, { status, rejectionReason });
      }

      setFeedback({ type: "success", message: "지원서 상태가 업데이트되었습니다." });
      await loadOverview();
    } catch (decisionError) {
      setFeedback({ type: "error", message: decisionError.message });
    } finally {
      setSavingKey("");
    }
  }

  if (loading) {
    return <LoadingScreen message="지원서 관리 화면을 불러오는 중입니다." />;
  }

  if (!overview) {
    return (
      <EmptyState
        title="프로젝트 관리 정보를 불러오지 못했습니다."
        description={error || "잠시 뒤 다시 시도해 주세요."}
        action={
          <Link className="ghost-button button-small" to="/student/projects/me">
            내 모집글로 돌아가기
          </Link>
        }
      />
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={`${overview.post.title} 지원서 관리`}
        description="학생 지원서와 강사 지원 요청을 검토하고 수락 또는 반려를 처리할 수 있습니다."
        actions={
          <Link className="ghost-button button-small" to={`/projects/${overview.post.id}`}>
            프로젝트 상세 보기
          </Link>
        }
      />

      {feedback.message ? (
        <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
          {feedback.message}
        </div>
      ) : null}

      <section className="panel">
        <div className="spread-row">
          <h2>프로젝트 요약</h2>
          <StatusBadge value={overview.post.status} />
        </div>
        <div className="project-meta tight">
          <small>PM: {overview.post.ownerName}</small>
          <small>기술 스택: {overview.post.techStack}</small>
          <small>모집 마감: {formatDate(overview.post.recruitUntil)}</small>
          <small>작성일: {formatDateTime(overview.post.createdAt)}</small>
        </div>
        <p className="detail-description">{overview.post.description}</p>
      </section>

      <section className="panel">
        <div className="spread-row">
          <h2>학생 지원서</h2>
          <small>{overview.projectApplications.length}건</small>
        </div>

        {overview.projectApplications.length === 0 ? (
          <EmptyState
            title="아직 학생 지원서가 없습니다."
            description="프로젝트 상세 화면에서 제출된 학생 지원서가 여기에 표시됩니다."
          />
        ) : (
          <div className="list-stack">
            {overview.projectApplications.map((application) => (
              <ProjectApplicationCard
                key={application.id}
                application={application}
                reason={getReason("project", application.id)}
                onReasonChange={(value) => setReason("project", application.id, value)}
                onAccept={() => handleDecision("project", application.id, "ACCEPTED")}
                onReject={() => handleDecision("project", application.id, "REJECTED")}
                saving={savingKey === reasonKey("project", application.id)}
              />
            ))}
          </div>
        )}
      </section>

      <section className="panel">
        <div className="spread-row">
          <h2>강사 지원 요청</h2>
          <small>{overview.mentorApplications.length}건</small>
        </div>

        {overview.mentorApplications.length === 0 ? (
          <EmptyState
            title="아직 강사 지원 요청이 없습니다."
            description="강사가 프로젝트 상세 화면에서 제출한 지원 요청이 여기에 표시됩니다."
          />
        ) : (
          <div className="list-stack">
            {overview.mentorApplications.map((application) => (
              <SupportApplicationCard
                key={application.id}
                application={application}
                reason={getReason("mentor", application.id)}
                onReasonChange={(value) => setReason("mentor", application.id, value)}
                onAccept={() => handleDecision("mentor", application.id, "ACCEPTED")}
                onReject={() => handleDecision("mentor", application.id, "REJECTED")}
                saving={savingKey === reasonKey("mentor", application.id)}
              />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function ProjectApplicationCard({
  application,
  reason,
  onReasonChange,
  onAccept,
  onReject,
  saving,
}) {
  const canDecide = application.status === "SUBMITTED";

  return (
    <article className="info-card application-card">
      <div className="spread-row">
        <div className="list-stack compact-list">
          <strong>{application.applicantName}</strong>
          <span>{application.projectPositionName}</span>
        </div>
        <StatusBadge value={application.status} />
      </div>

      <div className="project-meta tight">
        <small>제출일: {formatDateTime(application.createdAt)}</small>
        <small>검토자: {application.reviewedByName || "-"}</small>
        <small>검토 시각: {formatDateTime(application.reviewedAt)}</small>
      </div>

      <div className="section-divider" />
      <strong>지원 동기</strong>
      <p>{application.motivation}</p>

      {application.courseHistory ? (
        <>
          <strong>수강 이력</strong>
          <p>{application.courseHistory}</p>
        </>
      ) : null}

      {application.certifications ? (
        <>
          <strong>자격증 및 증빙</strong>
          <p>{application.certifications}</p>
        </>
      ) : null}

      <strong>기술 스택</strong>
      <p>{application.techStack}</p>

      {application.portfolioUrl ? (
        <p>
          <a href={application.portfolioUrl} target="_blank" rel="noreferrer">
            포트폴리오 열기
          </a>
        </p>
      ) : null}

      <strong>자기소개</strong>
      <p>{application.selfIntroduction}</p>

      {application.decisionReason ? (
        <p className="muted-text">처리 사유: {application.decisionReason}</p>
      ) : null}

      {canDecide ? (
        <>
          <FormField label="반려 사유" hint="반려할 때만 입력하면 됩니다.">
            <textarea value={reason} onChange={(event) => onReasonChange(event.target.value)} rows={3} />
          </FormField>

          <div className="button-row">
            <button className="primary-button subtle button-small" type="button" onClick={onAccept} disabled={saving}>
              수락
            </button>
            <button className="danger-button button-small" type="button" onClick={onReject} disabled={saving}>
              반려
            </button>
          </div>
        </>
      ) : null}
    </article>
  );
}

function SupportApplicationCard({
  application,
  reason,
  onReasonChange,
  onAccept,
  onReject,
  saving,
}) {
  const canDecide = application.status === "SUBMITTED";

  return (
    <article className="info-card application-card">
      <div className="spread-row">
        <div className="list-stack compact-list">
          <strong>{application.applicantName}</strong>
          <span>강사 지원 요청</span>
        </div>
        <StatusBadge value={application.status} />
      </div>

      <div className="project-meta tight">
        <small>제출일: {formatDateTime(application.createdAt)}</small>
        <small>검토자: {application.reviewedByName || "-"}</small>
        <small>검토 시각: {formatDateTime(application.reviewedAt)}</small>
      </div>

      <div className="section-divider" />
      <strong>전문 분야 요약</strong>
      <p>{application.expertiseSummary}</p>

      {application.mentoringExperience ? (
        <>
          <strong>관련 경험</strong>
          <p>{application.mentoringExperience}</p>
        </>
      ) : null}

      <strong>지원 계획</strong>
      <p>{application.supportPlan}</p>

      {application.portfolioUrl ? (
        <p>
          <a href={application.portfolioUrl} target="_blank" rel="noreferrer">
            포트폴리오 열기
          </a>
        </p>
      ) : null}

      {application.decisionReason ? (
        <p className="muted-text">처리 사유: {application.decisionReason}</p>
      ) : null}

      {canDecide ? (
        <>
          <FormField label="반려 사유" hint="반려할 때만 입력하면 됩니다.">
            <textarea value={reason} onChange={(event) => onReasonChange(event.target.value)} rows={3} />
          </FormField>

          <div className="button-row">
            <button className="primary-button subtle button-small" type="button" onClick={onAccept} disabled={saving}>
              수락
            </button>
            <button className="danger-button button-small" type="button" onClick={onReject} disabled={saving}>
              반려
            </button>
          </div>
        </>
      ) : null}
    </article>
  );
}
