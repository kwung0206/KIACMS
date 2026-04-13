import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { deleteMyProjectPost, fetchProjectBoard, fetchProjectDetail } from "../../../api/projectApi";
import ConfirmModal from "../../../components/common/ConfirmModal";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { useAuth } from "../../../hooks/useAuth";
import { formatDate, formatDateTime } from "../../../utils/date";

const STATUS_OPTIONS = [
  { value: "", label: "전체" },
  { value: "OPEN", label: "모집 중" },
  { value: "CLOSED", label: "마감" },
];

const CONTACT_METHOD_LABELS = {
  EMAIL: "이메일",
  OPEN_CHAT: "오픈채팅",
  DISCORD: "디스코드",
  NOTION: "노션",
  GOOGLE_FORM: "구글 폼",
  OTHER: "기타",
};

function getContactHref(contactMethod, contactValue) {
  if (!contactValue) {
    return null;
  }

  if (contactMethod === "EMAIL") {
    return `mailto:${contactValue}`;
  }

  if (/^https?:\/\//i.test(contactValue)) {
    return contactValue;
  }

  return null;
}

export default function ProjectBoardPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [status, setStatus] = useState("");
  const [posts, setPosts] = useState([]);
  const [selectedPostId, setSelectedPostId] = useState("");
  const [selectedPost, setSelectedPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState("");
  const [detailError, setDetailError] = useState("");
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let active = true;

    async function loadPosts() {
      setLoading(true);
      setError("");
      setFeedback({ type: "", message: "" });

      try {
        const response = await fetchProjectBoard(status || undefined);
        if (!active) {
          return;
        }

        setPosts(response);
        setSelectedPostId((current) => {
          if (current && response.some((post) => post.id === current)) {
            return current;
          }

          return response[0]?.id || "";
        });
      } catch (loadError) {
        if (!active) {
          return;
        }

        setPosts([]);
        setSelectedPostId("");
        setSelectedPost(null);
        setError(loadError.message);
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadPosts();
    return () => {
      active = false;
    };
  }, [status]);

  useEffect(() => {
    let active = true;

    async function loadDetail() {
      if (!selectedPostId) {
        setSelectedPost(null);
        return;
      }

      setDetailLoading(true);
      setDetailError("");

      try {
        const response = await fetchProjectDetail(selectedPostId);
        if (!active) {
          return;
        }

        setSelectedPost(response);
      } catch (loadError) {
        if (!active) {
          return;
        }

        setSelectedPost(null);
        setDetailError(loadError.message);
      } finally {
        if (active) {
          setDetailLoading(false);
        }
      }
    }

    loadDetail();
    return () => {
      active = false;
    };
  }, [selectedPostId]);

  const actions = useMemo(() => {
    if (user?.roleType === "STUDENT") {
      return (
        <div className="page-action-group">
          <Link className="primary-button button-small uniform-action-button" to="/student/projects/new">
            새 모집글 작성
          </Link>
          <Link className="ghost-button button-small uniform-action-button" to="/student/projects/me">
            내 모집글
          </Link>
          <Link className="ghost-button button-small uniform-action-button" to="/student/applications">
            내 지원 현황
          </Link>
        </div>
      );
    }

    if (user?.roleType === "INSTRUCTOR") {
      return (
        <Link className="ghost-button button-small uniform-action-button" to="/instructor/project-mentoring">
          프로젝트 지원 이력
        </Link>
      );
    }

    if (user?.roleType === "ROOT") {
      return (
        <Link className="primary-button subtle button-small uniform-action-button" to="/root/projects">
          프로젝트 삭제 관리
        </Link>
      );
    }

    return null;
  }, [user?.roleType]);

  async function handleDeleteSelectedPost() {
    if (!selectedPost) {
      return;
    }

    setDeleting(true);

    try {
      await deleteMyProjectPost(selectedPost.id);
      setFeedback({ type: "success", message: "모집글이 삭제되었습니다." });
      setDeleteModalOpen(false);
      const refreshed = await fetchProjectBoard(status || undefined);
      setPosts(refreshed);
      setSelectedPostId(refreshed[0]?.id || "");
    } catch (deleteError) {
      setFeedback({ type: "error", message: deleteError.message });
    } finally {
      setDeleting(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="프로젝트 게시판을 불러오는 중입니다." />;
  }

  const selectedSummary = posts.find((post) => post.id === selectedPostId) || null;
  const isOwner =
    Boolean(selectedPost && user && selectedPost.ownerId === user.id) && user?.roleType === "STUDENT";
  const contactHref = getContactHref(selectedPost?.contactMethod, selectedPost?.contactValue);

  return (
    <>
      <div className="page-stack">
        <PageHeader
          title="프로젝트 게시판"
          description="모집글을 목록 중심으로 확인하고, 선택한 프로젝트의 상세 정보와 지원 액션을 바로 볼 수 있습니다."
          actions={actions}
        />

        <div className="panel form-grid compact-grid">
          <FormField label="모집 상태">
            <select value={status} onChange={(event) => setStatus(event.target.value)}>
              {STATUS_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </FormField>
        </div>

        {error ? <div className="form-alert error">{error}</div> : null}
        {feedback.message ? (
          <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
            {feedback.message}
          </div>
        ) : null}

        {posts.length === 0 ? (
          <EmptyState
            title="조건에 맞는 프로젝트 모집글이 없습니다."
            description="필터를 바꾸거나 조금 뒤 다시 확인해 주세요."
            action={
              user?.roleType === "STUDENT" ? (
                <Link className="primary-button button-small uniform-action-button" to="/student/projects/new">
                  새 모집글 작성
                </Link>
              ) : null
            }
          />
        ) : (
          <section className="project-board-layout">
            <article className="panel project-list-panel">
              <div className="section-title-row">
                <h2>모집글 목록</h2>
                <span className="muted-text">{posts.length}건</span>
              </div>

              <div className="list-stack compact-list">
                {posts.map((post) => (
                  <button
                    key={post.id}
                    type="button"
                    className={`project-list-row ${post.id === selectedPostId ? "project-list-row-active" : ""}`}
                    onClick={() => setSelectedPostId(post.id)}
                  >
                    <div className="project-list-main">
                      <strong>{post.title}</strong>
                      <span>{post.ownerName}</span>
                    </div>
                    <div className="project-list-meta">
                      <small>마감일 {formatDate(post.recruitUntil)}</small>
                      <StatusBadge value={post.status} />
                    </div>
                  </button>
                ))}
              </div>
            </article>

            <article className="panel project-detail-preview">
              {!selectedSummary ? (
                <EmptyState
                  title="프로젝트를 선택해 주세요."
                  description="왼쪽 목록에서 프로젝트를 선택하면 상세 정보를 볼 수 있습니다."
                />
              ) : detailLoading ? (
                <LoadingScreen message="프로젝트 상세를 불러오는 중입니다." />
              ) : selectedPost ? (
                <div className="page-stack compact-page-stack">
                  <div className="spread-row">
                    <div className="list-stack compact-list">
                      <h2>{selectedPost.title}</h2>
                      <span className="muted-text">
                        {selectedPost.ownerName} · 모집 마감 {formatDate(selectedPost.recruitUntil)}
                      </span>
                    </div>
                    <StatusBadge value={selectedPost.status} />
                  </div>

                  <div className="project-detail-summary">
                    <div>
                      <span>작성자</span>
                      <strong>{selectedPost.ownerName}</strong>
                    </div>
                    <div>
                      <span>기술 스택</span>
                      <strong>{selectedPost.techStack}</strong>
                    </div>
                    <div>
                      <span>예상 기간</span>
                      <strong>{selectedPost.durationText}</strong>
                    </div>
                    <div>
                      <span>작성일</span>
                      <strong>{formatDateTime(selectedPost.createdAt)}</strong>
                    </div>
                  </div>

                  <div className="detail-list">
                    <div>
                      <dt>프로젝트 소개</dt>
                      <dd>{selectedPost.description}</dd>
                    </div>
                    <div>
                      <dt>목표</dt>
                      <dd>{selectedPost.goal}</dd>
                    </div>
                    <div>
                      <dt>연락 방식</dt>
                      <dd>
                        {CONTACT_METHOD_LABELS[selectedPost.contactMethod] || selectedPost.contactMethod}
                        {" · "}
                        {contactHref ? (
                          <a href={contactHref} target="_blank" rel="noreferrer">
                            {selectedPost.contactValue}
                          </a>
                        ) : (
                          selectedPost.contactValue
                        )}
                      </dd>
                    </div>
                    <div>
                      <dt>PM 소개</dt>
                      <dd>{selectedPost.pmIntroduction}</dd>
                    </div>
                    <div>
                      <dt>PM 경험 및 강점</dt>
                      <dd>{selectedPost.pmBackground}</dd>
                    </div>
                  </div>

                  <div className="list-stack compact-list">
                    <strong>모집 포지션</strong>
                    {selectedPost.positions.map((position) => (
                      <div key={position.id} className="info-card">
                        <div className="spread-row">
                          <strong>{position.name}</strong>
                          <small>
                            모집 {position.capacity}명 · 남은 자리 {position.remainingSlots}명
                          </small>
                        </div>
                        <p>{position.description || "포지션 설명이 아직 등록되지 않았습니다."}</p>
                        <small>{position.requiredSkills || "필수 역량 메모가 없습니다."}</small>
                      </div>
                    ))}
                  </div>

                  <div className="page-action-group">
                    <Link className="ghost-button button-small" to={`/projects/${selectedPost.id}`}>
                      상세 페이지 열기
                    </Link>
                    {isOwner ? (
                      <>
                        <Link
                          className="primary-button button-small"
                          to={`/student/projects/${selectedPost.id}/manage`}
                        >
                          지원서 관리
                        </Link>
                        <button
                          className="danger-button button-small"
                          type="button"
                          onClick={() => setDeleteModalOpen(true)}
                        >
                          모집글 삭제
                        </button>
                      </>
                    ) : null}
                    {!isOwner && user?.roleType === "STUDENT" ? (
                      <button
                        className="primary-button button-small"
                        type="button"
                        onClick={() => navigate(`/projects/${selectedPost.id}`)}
                      >
                        지원하러 가기
                      </button>
                    ) : null}
                  </div>
                </div>
              ) : (
                <EmptyState
                  title="상세 정보를 불러오지 못했습니다."
                  description={detailError || "잠시 뒤 다시 시도해 주세요."}
                />
              )}
            </article>
          </section>
        )}
      </div>

      <ConfirmModal
        open={deleteModalOpen}
        title="모집글 삭제"
        description="선택한 모집글을 삭제하시겠습니까? 삭제 후에는 게시판에서 숨겨집니다."
        confirmLabel={deleting ? "삭제 중..." : "삭제"}
        cancelLabel="취소"
        tone="danger"
        onConfirm={handleDeleteSelectedPost}
        onCancel={() => setDeleteModalOpen(false)}
      />
    </>
  );
}
