import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  deleteProjectApplication,
  fetchMyProjectApplications,
  updateProjectApplication,
  withdrawProjectApplication,
} from "../../../api/projectApi";
import ConfirmModal from "../../../components/common/ConfirmModal";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
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

const emptyForm = {
  motivation: "",
  courseHistory: "",
  certifications: "",
  techStack: "",
  portfolioUrl: "",
  selfIntroduction: "",
};

function toForm(application) {
  return {
    motivation: application?.motivation || "",
    courseHistory: application?.courseHistory || "",
    certifications: application?.certifications || "",
    techStack: application?.techStack || "",
    portfolioUrl: application?.portfolioUrl || "",
    selfIntroduction: application?.selfIntroduction || "",
  };
}

export default function MyApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [selectedId, setSelectedId] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [withdrawingId, setWithdrawingId] = useState("");
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  useEffect(() => {
    loadApplications();
  }, []);

  async function loadApplications() {
    setLoading(true);
    setError("");

    try {
      const response = await fetchMyProjectApplications();
      setApplications(response);
      setSelectedId((current) => {
        if (current && response.some((application) => application.id === current)) {
          return current;
        }

        return response[0]?.id || "";
      });
    } catch (loadError) {
      setApplications([]);
      setSelectedId("");
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }

  const filteredApplications = useMemo(() => {
    if (!statusFilter) {
      return applications;
    }

    return applications.filter((application) => application.status === statusFilter);
  }, [applications, statusFilter]);

  useEffect(() => {
    setSelectedId((current) => {
      if (current && filteredApplications.some((application) => application.id === current)) {
        return current;
      }

      return filteredApplications[0]?.id || "";
    });
  }, [filteredApplications]);

  const selectedApplication = useMemo(
    () =>
      filteredApplications.find((application) => application.id === selectedId) ||
      applications.find((application) => application.id === selectedId) ||
      null,
    [applications, filteredApplications, selectedId],
  );

  useEffect(() => {
    setForm(toForm(selectedApplication));
    setEditing(false);
  }, [selectedApplication?.id]);

  function updateForm(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleWithdraw(applicationId) {
    setFeedback({ type: "", message: "" });
    setWithdrawingId(applicationId);

    try {
      await withdrawProjectApplication(applicationId);
      setFeedback({ type: "success", message: "지원서가 철회되었습니다." });
      await loadApplications();
    } catch (withdrawError) {
      setFeedback({ type: "error", message: withdrawError.message });
    } finally {
      setWithdrawingId("");
    }
  }

  async function handleUpdate(event) {
    event.preventDefault();
    if (!selectedApplication) {
      return;
    }

    setFeedback({ type: "", message: "" });
    setSaving(true);

    try {
      await updateProjectApplication(selectedApplication.id, form);
      setFeedback({
        type: "success",
        message:
          selectedApplication.status === "SUBMITTED"
            ? "지원서가 수정되었습니다."
            : "지원서가 수정되었고 다시 제출 상태로 전환되었습니다.",
      });
      await loadApplications();
      setEditing(false);
    } catch (updateError) {
      setFeedback({ type: "error", message: updateError.message });
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!selectedApplication) {
      return;
    }

    setSaving(true);
    setFeedback({ type: "", message: "" });

    try {
      await deleteProjectApplication(selectedApplication.id);
      setDeleteModalOpen(false);
      setFeedback({ type: "success", message: "지원서가 삭제되었습니다." });
      await loadApplications();
    } catch (deleteError) {
      setFeedback({ type: "error", message: deleteError.message });
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="프로젝트 지원 현황을 불러오는 중입니다." />;
  }

  const canEdit = selectedApplication && selectedApplication.status !== "ACCEPTED";

  return (
    <>
      <div className="page-stack">
        <PageHeader
          title="내 지원 현황"
          description="제출한 지원서를 조회하고, 허용된 상태에서는 수정·삭제·철회까지 처리할 수 있습니다."
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
            title="표시할 지원서가 없습니다."
            description="프로젝트 게시판에서 관심 있는 모집글에 지원하면 이곳에서 상태를 확인할 수 있습니다."
            action={
              <Link className="primary-button button-small" to="/projects">
                프로젝트 둘러보기
              </Link>
            }
          />
        ) : (
          <section className="project-board-layout">
            <article className="panel project-list-panel">
              <div className="section-title-row">
                <h2>지원서 목록</h2>
                <span className="muted-text">{filteredApplications.length}건</span>
              </div>

              <div className="list-stack compact-list">
                {filteredApplications.map((application) => (
                  <button
                    key={application.id}
                    type="button"
                    className={`project-list-row ${application.id === selectedId ? "project-list-row-active" : ""}`}
                    onClick={() => setSelectedId(application.id)}
                  >
                    <div className="project-list-main">
                      <strong>{application.projectPostTitle}</strong>
                      <span>{application.projectPositionName}</span>
                    </div>
                    <div className="project-list-meta">
                      <small>{formatDateTime(application.createdAt)}</small>
                      <StatusBadge value={application.status} />
                    </div>
                  </button>
                ))}
              </div>
            </article>

            <article className="panel project-detail-preview">
              {!selectedApplication ? (
                <EmptyState
                  title="지원서를 선택해 주세요."
                  description="왼쪽 목록에서 지원서를 선택하면 상세 내용과 수정 폼을 볼 수 있습니다."
                />
              ) : editing ? (
                <form className="form-stack" onSubmit={handleUpdate}>
                  <div className="spread-row">
                    <div className="list-stack compact-list">
                      <h2>지원서 수정</h2>
                      <span className="muted-text">
                        {selectedApplication.projectPostTitle} · {selectedApplication.projectPositionName}
                      </span>
                    </div>
                    <StatusBadge value={selectedApplication.status} />
                  </div>

                  {selectedApplication.status !== "SUBMITTED" ? (
                    <div className="form-alert">
                      수정 후 저장하면 지원서 상태가 다시 제출됨으로 변경됩니다.
                    </div>
                  ) : null}

                  <FormField label="지원 동기">
                    <textarea
                      name="motivation"
                      value={form.motivation}
                      onChange={updateForm}
                      rows={4}
                      required
                    />
                  </FormField>

                  <FormField label="수강 이력">
                    <textarea
                      name="courseHistory"
                      value={form.courseHistory}
                      onChange={updateForm}
                      rows={3}
                    />
                  </FormField>

                  <FormField label="자격증 및 증빙">
                    <textarea
                      name="certifications"
                      value={form.certifications}
                      onChange={updateForm}
                      rows={3}
                    />
                  </FormField>

                  <FormField label="기술 스택">
                    <textarea
                      name="techStack"
                      value={form.techStack}
                      onChange={updateForm}
                      rows={3}
                      required
                    />
                  </FormField>

                  <FormField label="포트폴리오 URL">
                    <input name="portfolioUrl" value={form.portfolioUrl} onChange={updateForm} />
                  </FormField>

                  <FormField label="자기소개">
                    <textarea
                      name="selfIntroduction"
                      value={form.selfIntroduction}
                      onChange={updateForm}
                      rows={4}
                      required
                    />
                  </FormField>

                  <div className="button-row">
                    <button className="primary-button button-small" type="submit" disabled={saving}>
                      {saving ? "저장 중..." : "수정 저장"}
                    </button>
                    <button
                      className="ghost-button button-small"
                      type="button"
                      onClick={() => {
                        setForm(toForm(selectedApplication));
                        setEditing(false);
                      }}
                    >
                      취소
                    </button>
                  </div>
                </form>
              ) : (
                <div className="page-stack compact-page-stack">
                  <div className="spread-row">
                    <div className="list-stack compact-list">
                      <h2>{selectedApplication.projectPostTitle}</h2>
                      <span className="muted-text">{selectedApplication.projectPositionName}</span>
                    </div>
                    <StatusBadge value={selectedApplication.status} />
                  </div>

                  <div className="project-detail-summary">
                    <div>
                      <span>지원일</span>
                      <strong>{formatDateTime(selectedApplication.createdAt)}</strong>
                    </div>
                    <div>
                      <span>검토자</span>
                      <strong>{selectedApplication.reviewedByName || "-"}</strong>
                    </div>
                    <div>
                      <span>검토 시각</span>
                      <strong>{formatDateTime(selectedApplication.reviewedAt)}</strong>
                    </div>
                    <div>
                      <span>철회 시각</span>
                      <strong>{formatDateTime(selectedApplication.withdrawnAt)}</strong>
                    </div>
                  </div>

                  <div className="detail-list">
                    <div>
                      <dt>지원 동기</dt>
                      <dd>{selectedApplication.motivation}</dd>
                    </div>
                    <div>
                      <dt>수강 이력</dt>
                      <dd>{selectedApplication.courseHistory || "-"}</dd>
                    </div>
                    <div>
                      <dt>자격증 및 증빙</dt>
                      <dd>{selectedApplication.certifications || "-"}</dd>
                    </div>
                    <div>
                      <dt>기술 스택</dt>
                      <dd>{selectedApplication.techStack}</dd>
                    </div>
                    <div>
                      <dt>포트폴리오</dt>
                      <dd>
                        {selectedApplication.portfolioUrl ? (
                          <a href={selectedApplication.portfolioUrl} target="_blank" rel="noreferrer">
                            링크 열기
                          </a>
                        ) : (
                          "-"
                        )}
                      </dd>
                    </div>
                    <div>
                      <dt>자기소개</dt>
                      <dd>{selectedApplication.selfIntroduction}</dd>
                    </div>
                  </div>

                  {selectedApplication.decisionReason ? (
                    <div className="info-card">
                      <strong>처리 사유</strong>
                      <p>{selectedApplication.decisionReason}</p>
                    </div>
                  ) : null}

                  <div className="page-action-group">
                    <Link className="ghost-button button-small" to={`/projects/${selectedApplication.projectPostId}`}>
                      모집글 보기
                    </Link>
                    {canEdit ? (
                      <>
                        <button
                          className="primary-button button-small"
                          type="button"
                          onClick={() => setEditing(true)}
                        >
                          지원서 수정
                        </button>
                        <button
                          className="danger-button button-small"
                          type="button"
                          onClick={() => setDeleteModalOpen(true)}
                        >
                          지원서 삭제
                        </button>
                      </>
                    ) : null}
                    {selectedApplication.status === "SUBMITTED" ? (
                      <button
                        className="ghost-button button-small"
                        type="button"
                        onClick={() => handleWithdraw(selectedApplication.id)}
                        disabled={withdrawingId === selectedApplication.id}
                      >
                        {withdrawingId === selectedApplication.id ? "철회 중..." : "지원 철회"}
                      </button>
                    ) : null}
                  </div>
                </div>
              )}
            </article>
          </section>
        )}
      </div>

      <ConfirmModal
        open={deleteModalOpen}
        title="지원서 삭제"
        description="선택한 지원서를 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다."
        confirmLabel={saving ? "삭제 중..." : "삭제"}
        cancelLabel="취소"
        tone="danger"
        onConfirm={handleDelete}
        onCancel={() => setDeleteModalOpen(false)}
      />
    </>
  );
}
