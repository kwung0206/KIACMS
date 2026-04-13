import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  deleteMyProjectPost,
  fetchProjectDetail,
  submitMentorApplication,
  submitProjectApplication,
} from "../../../api/projectApi";
import ConfirmModal from "../../../components/common/ConfirmModal";
import FormField from "../../../components/common/FormField";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { useAuth } from "../../../hooks/useAuth";
import { formatDate, formatDateTime } from "../../../utils/date";
import ProjectAiInsightPanel from "../components/ProjectAiInsightPanel";

const CONTACT_METHOD_LABELS = {
  EMAIL: "이메일",
  OPEN_CHAT: "오픈채팅",
  DISCORD: "디스코드",
  NOTION: "노션",
  GOOGLE_FORM: "구글 폼",
  OTHER: "기타",
};

const initialApplicationForm = {
  positionId: "",
  motivation: "",
  courseHistory: "",
  certifications: "",
  techStack: "",
  portfolioUrl: "",
  selfIntroduction: "",
};

const initialSupportForm = {
  expertiseSummary: "",
  mentoringExperience: "",
  portfolioUrl: "",
  supportPlan: "",
};

function getDefaultPositionId(positions) {
  return (
    positions.find((position) => position.remainingSlots > 0)?.id ||
    positions[0]?.id ||
    ""
  );
}

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

export default function ProjectDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [saving, setSaving] = useState("");
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [applicationForm, setApplicationForm] = useState(initialApplicationForm);
  const [supportForm, setSupportForm] = useState(initialSupportForm);

  const isOwner = useMemo(
    () => Boolean(post && user && post.ownerId === user.id),
    [post, user],
  );
  const selectedPosition = useMemo(
    () => post?.positions?.find((position) => position.id === applicationForm.positionId) ?? null,
    [post, applicationForm.positionId],
  );
  const canStudentApply = user?.roleType === "STUDENT" && !isOwner;
  const canInstructorSupport = user?.roleType === "INSTRUCTOR" && !isOwner;
  const isRecruiting = post?.status === "OPEN";
  const canUseProjectAi = isOwner && user?.roleType === "STUDENT";

  useEffect(() => {
    let active = true;

    async function loadPost() {
      setLoading(true);
      setError("");

      try {
        const response = await fetchProjectDetail(postId);

        if (!active) {
          return;
        }

        setPost(response);
        setApplicationForm((current) => ({
          ...current,
          positionId: current.positionId || getDefaultPositionId(response.positions),
        }));
      } catch (loadError) {
        if (!active) {
          return;
        }

        setPost(null);
        setError(loadError.message);
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadPost();
    return () => {
      active = false;
    };
  }, [postId]);

  function updateApplicationForm(event) {
    const { name, value } = event.target;
    setApplicationForm((current) => ({ ...current, [name]: value }));
  }

  function updateSupportForm(event) {
    const { name, value } = event.target;
    setSupportForm((current) => ({ ...current, [name]: value }));
  }

  async function handleStudentApply(event) {
    event.preventDefault();
    setFeedback({ type: "", message: "" });

    if (!applicationForm.positionId) {
      setFeedback({ type: "error", message: "지원할 포지션을 먼저 선택해 주세요." });
      return;
    }

    setSaving("student");

    try {
      const { positionId, ...payload } = applicationForm;
      await submitProjectApplication(positionId, payload);
      setApplicationForm({
        ...initialApplicationForm,
        positionId: getDefaultPositionId(post.positions),
      });
      setFeedback({
        type: "success",
        message: "지원서가 제출되었습니다. 내 지원 현황에서 결과를 확인할 수 있습니다.",
      });
    } catch (submitError) {
      setFeedback({ type: "error", message: submitError.message });
    } finally {
      setSaving("");
    }
  }

  async function handleSupportApply(event) {
    event.preventDefault();
    setFeedback({ type: "", message: "" });
    setSaving("support");

    try {
      await submitMentorApplication(postId, supportForm);
      setSupportForm(initialSupportForm);
      setFeedback({
        type: "success",
        message: "프로젝트 지원 요청이 제출되었습니다.",
      });
    } catch (submitError) {
      setFeedback({ type: "error", message: submitError.message });
    } finally {
      setSaving("");
    }
  }

  async function handleDeletePost() {
    if (!post) {
      return;
    }

    setSaving("delete");

    try {
      await deleteMyProjectPost(post.id);
      navigate("/student/projects/me", {
        replace: true,
        state: { message: "모집글이 삭제되었습니다." },
      });
    } catch (deleteError) {
      setFeedback({ type: "error", message: deleteError.message });
      setDeleteModalOpen(false);
    } finally {
      setSaving("");
    }
  }

  if (loading) {
    return <LoadingScreen message="프로젝트 상세를 불러오는 중입니다." />;
  }

  if (!post) {
    return (
      <EmptyState
        title="프로젝트 상세를 불러오지 못했습니다."
        description={error || "잠시 뒤 다시 시도해 주세요."}
        action={
          <Link className="ghost-button button-small" to="/projects">
            프로젝트 게시판으로
          </Link>
        }
      />
    );
  }

  const contactHref = getContactHref(post.contactMethod, post.contactValue);
  const submitDisabled =
    saving === "student" ||
    !isRecruiting ||
    !selectedPosition ||
    selectedPosition.remainingSlots === 0;
  const supportDisabled = saving === "support" || !isRecruiting;

  return (
    <>
      <div className="page-stack">
        <PageHeader
          title={post.title}
          description={`${post.ownerName} · 모집 마감 ${formatDate(post.recruitUntil)}`}
          actions={
            <div className="page-action-group">
              <Link className="ghost-button button-small" to="/projects">
                게시판으로
              </Link>
              {isOwner && user?.roleType === "STUDENT" ? (
                <>
                  <Link className="primary-button button-small" to={`/student/projects/${post.id}/manage`}>
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
              {user?.roleType === "ROOT" ? (
                <Link className="ghost-button button-small" to="/root/projects">
                  Root 삭제 관리
                </Link>
              ) : null}
            </div>
          }
        />

        {feedback.message ? (
          <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
            {feedback.message}
          </div>
        ) : null}

        {!isRecruiting ? (
          <div className="form-alert error">현재 이 모집글은 지원을 받지 않고 있습니다.</div>
        ) : null}

        {user?.roleType === "MENTOR" ? (
          <div className="form-alert">
            멘토 계정은 수강생 관리 중심 역할이므로 프로젝트 참여 액션은 제공되지 않습니다.
          </div>
        ) : null}

        <section className="detail-grid">
          <article className="panel">
            <div className="spread-row">
              <h2>프로젝트 개요</h2>
              <StatusBadge value={post.status} />
            </div>
            <dl className="detail-list">
              <div>
                <dt>목표</dt>
                <dd>{post.goal}</dd>
              </div>
              <div>
                <dt>기술 스택</dt>
                <dd>{post.techStack}</dd>
              </div>
              <div>
                <dt>예상 진행 기간</dt>
                <dd>{post.durationText}</dd>
              </div>
              <div>
                <dt>연락 방식</dt>
                <dd>
                  {CONTACT_METHOD_LABELS[post.contactMethod] || post.contactMethod}
                  {" · "}
                  {contactHref ? (
                    <a href={contactHref} target="_blank" rel="noreferrer">
                      {post.contactValue}
                    </a>
                  ) : (
                    post.contactValue
                  )}
                </dd>
              </div>
              <div>
                <dt>PM 소개</dt>
                <dd>{post.pmIntroduction}</dd>
              </div>
              <div>
                <dt>PM 경험 및 강점</dt>
                <dd>{post.pmBackground}</dd>
              </div>
              <div>
                <dt>등록일</dt>
                <dd>{formatDateTime(post.createdAt)}</dd>
              </div>
              {post.closedAt ? (
                <div>
                  <dt>마감 처리 시각</dt>
                  <dd>{formatDateTime(post.closedAt)}</dd>
                </div>
              ) : null}
            </dl>
            <p className="detail-description">{post.description}</p>
          </article>

          <article className="panel">
            <h2>모집 포지션</h2>
            <div className="list-stack">
              {post.positions.map((position) => (
                <div key={position.id} className="info-card">
                  <div className="spread-row">
                    <strong>{position.name}</strong>
                    <small>
                      수락 {position.acceptedCount}명 / 모집 {position.capacity}명 / 남은 자리{" "}
                      {position.remainingSlots}명
                    </small>
                  </div>
                  <p>{position.description || "포지션 설명이 아직 등록되지 않았습니다."}</p>
                  <span>{position.requiredSkills || "필수 역량 메모가 없습니다."}</span>
                </div>
              ))}
            </div>
          </article>
        </section>

        {canStudentApply ? (
          <form className="panel form-stack" onSubmit={handleStudentApply}>
            <div className="spread-row">
              <h2>학생 지원서 작성</h2>
              <Link className="ghost-button button-small" to="/student/applications">
                내 지원 현황
              </Link>
            </div>

            <FormField label="지원 포지션">
              <select
                name="positionId"
                value={applicationForm.positionId}
                onChange={updateApplicationForm}
                required
              >
                <option value="">포지션을 선택해 주세요.</option>
                {post.positions.map((position) => (
                  <option key={position.id} value={position.id}>
                    {position.name} · 남은 자리 {position.remainingSlots}명
                  </option>
                ))}
              </select>
            </FormField>

            {selectedPosition && selectedPosition.remainingSlots === 0 ? (
              <div className="form-alert error">
                선택한 포지션은 이미 마감되었습니다. 다른 포지션을 선택해 주세요.
              </div>
            ) : null}

            <FormField label="지원 동기">
              <textarea
                name="motivation"
                value={applicationForm.motivation}
                onChange={updateApplicationForm}
                rows={4}
                required
              />
            </FormField>

            <FormField label="수강 이력">
              <textarea
                name="courseHistory"
                value={applicationForm.courseHistory}
                onChange={updateApplicationForm}
                rows={3}
              />
            </FormField>

            <FormField label="자격증 및 증빙">
              <textarea
                name="certifications"
                value={applicationForm.certifications}
                onChange={updateApplicationForm}
                rows={3}
              />
            </FormField>

            <FormField label="기술 스택">
              <textarea
                name="techStack"
                value={applicationForm.techStack}
                onChange={updateApplicationForm}
                rows={3}
                required
              />
            </FormField>

            <FormField label="포트폴리오 URL">
              <input
                name="portfolioUrl"
                value={applicationForm.portfolioUrl}
                onChange={updateApplicationForm}
              />
            </FormField>

            <FormField label="자기소개">
              <textarea
                name="selfIntroduction"
                value={applicationForm.selfIntroduction}
                onChange={updateApplicationForm}
                rows={4}
                required
              />
            </FormField>

            <button className="primary-button button-small" type="submit" disabled={submitDisabled}>
              {saving === "student" ? "제출 중..." : "지원서 제출"}
            </button>
          </form>
        ) : null}

        {canInstructorSupport ? (
          <form className="panel form-stack" onSubmit={handleSupportApply}>
            <div className="spread-row">
              <h2>강사 프로젝트 지원</h2>
              <Link className="ghost-button button-small" to="/instructor/project-mentoring">
                지원 이력
              </Link>
            </div>

            <FormField label="전문 분야 요약">
              <textarea
                name="expertiseSummary"
                value={supportForm.expertiseSummary}
                onChange={updateSupportForm}
                rows={4}
                required
              />
            </FormField>

            <FormField label="관련 경험">
              <textarea
                name="mentoringExperience"
                value={supportForm.mentoringExperience}
                onChange={updateSupportForm}
                rows={3}
              />
            </FormField>

            <FormField label="포트폴리오 URL">
              <input
                name="portfolioUrl"
                value={supportForm.portfolioUrl}
                onChange={updateSupportForm}
              />
            </FormField>

            <FormField label="지원 계획">
              <textarea
                name="supportPlan"
                value={supportForm.supportPlan}
                onChange={updateSupportForm}
                rows={4}
                required
              />
            </FormField>

            <button className="primary-button button-small" type="submit" disabled={supportDisabled}>
              {saving === "support" ? "제출 중..." : "지원 요청 제출"}
            </button>
          </form>
        ) : null}

        <ProjectAiInsightPanel post={post} visible={canUseProjectAi} />
      </div>

      <ConfirmModal
        open={deleteModalOpen}
        title="모집글 삭제"
        description="이 모집글을 삭제하시겠습니까? 삭제 후에는 게시판과 상세 화면에서 숨겨집니다."
        confirmLabel={saving === "delete" ? "삭제 중..." : "삭제"}
        cancelLabel="취소"
        tone="danger"
        onConfirm={handleDeletePost}
        onCancel={() => setDeleteModalOpen(false)}
      />
    </>
  );
}
