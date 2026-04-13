import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { approveUser, fetchPendingApprovals, rejectUser } from "../../../api/adminApi";
import { fetchRootProjectPosts } from "../../../api/rootApi";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDateTime } from "../../../utils/date";
import { getRoleLabel } from "../../../utils/userLabels";

export default function RootHomePage() {
  const [pendingUsers, setPendingUsers] = useState([]);
  const [projects, setProjects] = useState([]);
  const [reasons, setReasons] = useState({});
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ type: "", message: "" });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);

    try {
      const [pendingResponse, projectResponse] = await Promise.all([
        fetchPendingApprovals(),
        fetchRootProjectPosts(),
      ]);

      setPendingUsers(pendingResponse);
      setProjects(projectResponse);
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function handleApprove(userId) {
    try {
      await approveUser(userId);
      setFeedback({ type: "success", message: "가입 신청이 승인되었습니다." });
      await loadData();
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    }
  }

  async function handleReject(userId) {
    const reason = (reasons[userId] || "").trim();
    if (!reason) {
      setFeedback({ type: "error", message: "반려 사유를 입력해 주세요." });
      return;
    }

    try {
      await rejectUser(userId, reason);
      setFeedback({ type: "success", message: "가입 신청이 반려되었습니다." });
      await loadData();
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    }
  }

  if (loading) {
    return <LoadingScreen message="Root 운영 화면을 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="Root 운영 홈"
        description="회원가입 승인, 수업 일정 관리, 프로젝트 삭제 관리까지 Root 권한에서 한눈에 확인할 수 있습니다."
        actions={
          <div className="page-action-group">
            <Link className="primary-button button-small" to="/root/courses">
              수업 일정 관리
            </Link>
            <Link className="ghost-button button-small" to="/root/projects">
              프로젝트 삭제 관리
            </Link>
          </div>
        }
      />

      {feedback.message ? (
        <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
          {feedback.message}
        </div>
      ) : null}

      <section className="stats-grid">
        <article className="panel stat-card">
          <span>승인 대기</span>
          <strong>{pendingUsers.length}</strong>
          <p>즉시 검토 가능한 가입 신청 건수입니다.</p>
        </article>
        <article className="panel stat-card">
          <span>활성 프로젝트</span>
          <strong>{projects.length}</strong>
          <p>삭제 검토가 가능한 프로젝트 수입니다.</p>
        </article>
        <article className="panel stat-card">
          <span>권한 범위</span>
          <strong>ROOT</strong>
          <p>학생 수강 매핑이 아니라 승인, 일정, 삭제 운영에 집중하는 최고 관리자 역할입니다.</p>
        </article>
      </section>

      <section className="panel">
        <div className="section-title-row">
          <h2>회원가입 신청 검토</h2>
          <Link className="text-button" to="/root/courses">
            수업 일정 관리로 이동
          </Link>
        </div>

        {pendingUsers.length === 0 ? (
          <EmptyState
            title="승인 대기 사용자가 없습니다."
            description="현재 처리할 가입 신청이 없습니다."
          />
        ) : (
          <div className="list-stack">
            {pendingUsers.map((user) => (
              <article key={user.id} className="info-card approval-request-card">
                <div className="spread-row approval-request-header">
                  <div className="approval-identity">
                    <strong>{user.name}</strong>
                    <span>{user.email}</span>
                  </div>
                  <StatusBadge value={user.status} />
                </div>

                <div className="meta-grid approval-meta">
                  <small>신청 역할: {getRoleLabel(user.roleType)}</small>
                  <small>신청 시각: {formatDateTime(user.createdAt)}</small>
                </div>

                <div className="approval-reason-box">
                  <FormField label="반려 사유" hint="반려할 때만 사용됩니다.">
                    <textarea
                      rows={3}
                      value={reasons[user.id] || ""}
                      onChange={(event) =>
                        setReasons((current) => ({ ...current, [user.id]: event.target.value }))
                      }
                    />
                  </FormField>

                  <div className="button-row">
                    <button
                      className="primary-button subtle button-small"
                      type="button"
                      onClick={() => handleApprove(user.id)}
                    >
                      승인
                    </button>
                    <button
                      className="danger-button button-small"
                      type="button"
                      onClick={() => handleReject(user.id)}
                    >
                      반려
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
