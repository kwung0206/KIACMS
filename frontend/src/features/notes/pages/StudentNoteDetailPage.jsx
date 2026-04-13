import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { summarizeNote } from "../../../api/aiApi";
import { deleteNote, fetchMyNoteDetail } from "../../../api/noteApi";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import { formatDateTime } from "../../../utils/date";

function SummaryList({ title, items }) {
  return (
    <div className="info-card">
      <strong>{title}</strong>
      <ul className="plain-list">
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

export default function StudentNoteDetailPage() {
  const navigate = useNavigate();
  const { noteId } = useParams();
  const [note, setNote] = useState(null);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [aiLoading, setAiLoading] = useState(false);

  useEffect(() => {
    loadNote();
  }, [noteId]);

  async function loadNote() {
    try {
      const response = await fetchMyNoteDetail(noteId);
      setNote(response);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm("정리글을 삭제하시겠습니까?")) {
      return;
    }

    await deleteNote(noteId);
    navigate("/student/notes", { replace: true });
  }

  async function handleAiSummary() {
    setAiLoading(true);
    try {
      const response = await summarizeNote(noteId);
      setSummary(response);
    } finally {
      setAiLoading(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="정리글 상세를 불러오는 중..." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={note.title}
        description={`${note.courseTitle}${note.sessionTitle ? ` · ${note.sessionTitle}` : ""}`}
        actions={
          <div className="inline-actions">
            <Link className="ghost-button" to={`/student/notes/${note.id}/edit`}>
              수정
            </Link>
            <button className="danger-button" type="button" onClick={handleDelete}>
              삭제
            </button>
          </div>
        }
      />

      <section className="detail-grid">
        <article className="panel">
          <div className="section-title-row">
            <h2>정리 내용</h2>
            <small className="muted-text">{formatDateTime(note.updatedAt)} 수정</small>
          </div>
          <div className="info-card">
            <strong>과정/회차</strong>
            <span>
              {note.courseTitle}
              {note.sessionTitle ? ` · ${note.sessionTitle}` : ""}
            </span>
          </div>
          <pre className="note-content">{note.content}</pre>
        </article>

        <article className="panel">
          <h2>태그된 강사</h2>
          {note.tags.length === 0 ? (
            <EmptyState
              title="태그된 강사가 없습니다."
              description="수정 화면에서 강사 UUID를 입력해 추가 태그할 수 있습니다."
            />
          ) : (
            <div className="list-stack">
              {note.tags.map((tag) => (
                <div key={tag.id} className="info-card">
                  <strong>{tag.instructorName}</strong>
                  <span>{tag.instructorId}</span>
                  <small>태그 시각: {formatDateTime(tag.taggedAt)}</small>
                </div>
              ))}
            </div>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="section-title-row">
          <h2>강사 코멘트 타임라인</h2>
        </div>

        {note.comments.length === 0 ? (
          <p className="muted-text">아직 등록된 강사 코멘트가 없습니다.</p>
        ) : (
          <div className="timeline-list">
            {note.comments.map((comment) => (
              <div key={comment.id} className="timeline-item">
                <div className="timeline-dot" />
                <div className="info-card">
                  <div className="spread-row">
                    <strong>
                      {comment.authorName} ({comment.authorRole})
                    </strong>
                    <small>{formatDateTime(comment.createdAt)}</small>
                  </div>
                  <p>{comment.content}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="panel">
        <div className="section-title-row">
          <h2>AI 정리 요약</h2>
          <button
            className="primary-button"
            type="button"
            onClick={handleAiSummary}
            disabled={aiLoading}
          >
            {aiLoading ? "요약 생성 중..." : "AI 요약 생성"}
          </button>
        </div>

        {summary ? (
          <div className="ai-summary-grid">
            <div className="info-card">
              <strong>핵심 개념 요약</strong>
              <p>{summary.coreConceptSummary}</p>
            </div>
            <SummaryList title="복습 포인트" items={summary.reviewPoints} />
            <SummaryList title="질문 포인트" items={summary.questionPoints} />
            <SummaryList title="놓치기 쉬운 개념" items={summary.easyToMissConcepts} />
          </div>
        ) : (
          <p className="muted-text">
            버튼을 누르면 현재 정리글을 바탕으로 복습용 AI 요약을 생성합니다.
          </p>
        )}
      </section>
    </div>
  );
}
