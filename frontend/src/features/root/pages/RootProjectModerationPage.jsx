import { useEffect, useState } from "react";
import {
  deleteRootProject,
  fetchRootProjectDeletionHistory,
  fetchRootProjectPosts,
} from "../../../api/rootApi";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDate, formatDateTime } from "../../../utils/date";

export default function RootProjectModerationPage() {
  const [posts, setPosts] = useState([]);
  const [history, setHistory] = useState([]);
  const [reasons, setReasons] = useState({});
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ type: "", message: "" });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    const [postsResponse, historyResponse] = await Promise.all([
      fetchRootProjectPosts(),
      fetchRootProjectDeletionHistory(),
    ]);
    setPosts(postsResponse);
    setHistory(historyResponse);
    setLoading(false);
  }

  async function handleDelete(postId) {
    const reason = (reasons[postId] || "").trim();
    if (!reason) {
      setFeedback({ type: "error", message: "삭제 사유를 입력해야 프로젝트를 삭제할 수 있습니다." });
      return;
    }

    await deleteRootProject(postId, reason);
    setFeedback({
      type: "success",
      message: "프로젝트가 삭제되었고, 삭제 사유가 PM에게 전달되었습니다.",
    });
    await loadData();
  }

  if (loading) {
    return <LoadingScreen message="프로젝트 관리 화면을 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="프로젝트 관리"
        description="root는 프로젝트에 신청하지 않고, 목록을 검토한 뒤 사유를 입력해 삭제만 수행합니다."
      />

      {feedback.message ? (
        <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
          {feedback.message}
        </div>
      ) : null}

      <section className="panel">
        <div className="section-title-row">
          <h2>현재 프로젝트 목록</h2>
          <span className="muted-text">{posts.length}건</span>
        </div>

        {posts.length === 0 ? (
          <EmptyState
            title="표시할 프로젝트가 없습니다."
            description="현재 활성 프로젝트가 없거나 모두 정리된 상태입니다."
          />
        ) : (
          <div className="list-stack">
            {posts.map((post) => (
              <article key={post.id} className="info-card">
                <div className="spread-row">
                  <div>
                    <strong>{post.title}</strong>
                    <span>{post.ownerName}</span>
                  </div>
                  <StatusBadge value={post.status} />
                </div>

                <div className="project-meta tight">
                  <small>기술 스택: {post.techStack}</small>
                  <small>모집 포지션: {post.positionCount}개</small>
                  <small>마감일: {formatDate(post.recruitUntil)}</small>
                </div>

                <FormField label="삭제 사유" hint="사유를 입력해야 PM에게 알림과 함께 전달됩니다.">
                  <textarea
                    rows={3}
                    value={reasons[post.id] || ""}
                    onChange={(event) =>
                      setReasons((current) => ({ ...current, [post.id]: event.target.value }))
                    }
                  />
                </FormField>

                <div className="button-row">
                  <button className="danger-button button-small" type="button" onClick={() => handleDelete(post.id)}>
                    프로젝트 삭제
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="panel">
        <div className="section-title-row">
          <h2>삭제 이력</h2>
          <span className="muted-text">{history.length}건</span>
        </div>

        {history.length === 0 ? (
          <EmptyState
            title="삭제 이력이 없습니다."
            description="프로젝트 삭제가 발생하면 이 영역에 기록이 남습니다."
          />
        ) : (
          <div className="list-stack">
            {history.map((item) => (
              <div key={item.id} className="info-card">
                <div className="spread-row">
                  <strong>{item.projectTitle}</strong>
                  <small>{formatDateTime(item.deletedAt)}</small>
                </div>
                <span>PM: {item.projectOwnerName}</span>
                <small>삭제 관리자: {item.deletedByName}</small>
                <p className="detail-description">{item.reason}</p>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
