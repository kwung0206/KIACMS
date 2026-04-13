import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  fetchMyMentorApplications,
  withdrawMentorApplication,
} from "../../../api/projectApi";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDateTime } from "../../../utils/date";

const STATUS_OPTIONS = [
  { value: "", label: "전체" },
  { value: "SUBMITTED", label: "제출됨" },
  { value: "ACCEPTED", label: "수락" },
  { value: "REJECTED", label: "반려" },
  { value: "WITHDRAWN", label: "철회됨" },
];

export default function MentorApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [withdrawingId, setWithdrawingId] = useState("");

  useEffect(() => {
    loadApplications();
  }, []);

  async function loadApplications() {
    setLoading(true);
    setError("");

    try {
      const response = await fetchMyMentorApplications();
      setApplications(response);
    } catch (loadError) {
      setApplications([]);
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleWithdraw(applicationId) {
    setFeedback({ type: "", message: "" });
    setWithdrawingId(applicationId);

    try {
      await withdrawMentorApplication(applicationId);
      setFeedback({ type: "success", message: "지원 요청이 철회되었습니다." });
      await loadApplications();
    } catch (withdrawError) {
      setFeedback({ type: "error", message: withdrawError.message });
    } finally {
      setWithdrawingId("");
    }
  }

  const filteredApplications = useMemo(() => {
    if (!statusFilter) {
      return applications;
    }

    return applications.filter((application) => application.status === statusFilter);
  }, [applications, statusFilter]);

  if (loading) {
    return <LoadingScreen message="프로젝트 지원 이력을 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="프로젝트 지원 이력"
        description="강사 계정으로 제출한 프로젝트 지원 요청을 확인하고, 제출 상태일 때는 철회할 수 있습니다."
        actions={
          <Link className="ghost-button button-small" to="/projects">
            프로젝트 게시판 보기
          </Link>
        }
      />

      <div className="panel inline-form">
        <label className="form-field">
          <span className="form-field-label">상태 필터</span>
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
            {STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error ? <div className="form-alert error">{error}</div> : null}
      {feedback.message ? (
        <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
          {feedback.message}
        </div>
      ) : null}

      {filteredApplications.length === 0 ? (
        <EmptyState
          title="표시할 지원 이력이 없습니다."
          description="프로젝트 상세 화면에서 제출한 강사 지원 요청이 여기에 표시됩니다."
          action={
            <Link className="primary-button button-small" to="/projects">
              프로젝트 둘러보기
            </Link>
          }
        />
      ) : (
        <div className="list-stack">
          {filteredApplications.map((application) => (
            <article key={application.id} className="panel info-card application-card">
              <div className="spread-row">
                <div className="list-stack compact-list">
                  <strong>{application.projectPostTitle}</strong>
                  <span>{application.applicantName}</span>
                </div>
                <StatusBadge value={application.status} />
              </div>

              <div className="project-meta tight">
                <small>제출일: {formatDateTime(application.createdAt)}</small>
                <small>검토자: {application.reviewedByName || "-"}</small>
                <small>검토 시각: {formatDateTime(application.reviewedAt)}</small>
              </div>

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

              <div className="inline-actions">
                <Link className="ghost-button button-small" to={`/projects/${application.projectPostId}`}>
                  프로젝트 보기
                </Link>
                {application.status === "SUBMITTED" ? (
                  <button
                    className="danger-button button-small"
                    type="button"
                    onClick={() => handleWithdraw(application.id)}
                    disabled={withdrawingId === application.id}
                  >
                    {withdrawingId === application.id ? "철회 중..." : "지원 철회"}
                  </button>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
