import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  createTaggedNoteComment,
  fetchTaggedNoteDetail,
} from "../../../api/noteApi";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import { formatDateTime } from "../../../utils/date";

export default function TaggedNoteDetailPage() {
  const { noteId } = useParams();
  const [note, setNote] = useState(null);
  const [comment, setComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    loadNote();
  }, [noteId]);

  async function loadNote() {
    try {
      const response = await fetchTaggedNoteDetail(noteId);
      setNote(response);
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setMessage("");

    try {
      const response = await createTaggedNoteComment(noteId, comment);
      setNote(response);
      setComment("");
      setMessage("코멘트가 저장되었습니다.");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="태그된 노트를 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={note.title}
        description={`${note.authorName} | ${note.courseTitle}`}
        actions={
          <Link className="ghost-button button-small" to="/instructor/tagged-notes">
            목록으로
          </Link>
        }
      />

      <section className="detail-grid">
        <article className="panel">
          <h2>학생 정리글</h2>
          <div className="info-card">
            <strong>회차 정보</strong>
            <span>{note.sessionTitle || "연결된 회차가 없습니다."}</span>
            {note.sessionInstructorName ? (
              <small>강사: {note.sessionInstructorName}</small>
            ) : null}
          </div>
          <pre className="note-content">{note.content}</pre>
        </article>

        <article className="panel">
          <h2>태그된 강사</h2>
          {note.tags.length === 0 ? (
            <EmptyState
              title="태그 정보가 없습니다."
              description="이 정리글에는 아직 강사 태그 기록이 없습니다."
            />
          ) : (
            <div className="list-stack">
              {note.tags.map((tag) => (
                <div key={tag.id} className="info-card">
                  <strong>{tag.instructorName}</strong>
                  <span>{tag.instructorId}</span>
                  <small>
                    {tag.taggedByName} 님이 {formatDateTime(tag.taggedAt)}에 태그
                  </small>
                </div>
              ))}
            </div>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="section-title-row">
          <h2>코멘트 타임라인</h2>
        </div>

        {note.comments.length === 0 ? (
          <p className="muted-text">아직 등록된 강사 코멘트가 없습니다.</p>
        ) : (
          <div className="timeline-list">
            {note.comments.map((item) => (
              <div key={item.id} className="timeline-item">
                <div className="timeline-dot" />
                <div className="info-card">
                  <div className="spread-row">
                    <strong>{item.authorName}</strong>
                    <small>{formatDateTime(item.createdAt)}</small>
                  </div>
                  <p>{item.content}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="panel">
        <h2>코멘트 작성</h2>
        <form className="form-stack" onSubmit={handleSubmit}>
          <FormField
            label="피드백"
            hint="학생이 바로 이해할 수 있도록 구체적으로 작성해 주세요."
          >
            <textarea
              rows={5}
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              required
            />
          </FormField>

          {message ? <div className="form-alert">{message}</div> : null}

          <button className="primary-button button-small" type="submit" disabled={saving}>
            {saving ? "저장 중..." : "코멘트 저장"}
          </button>
        </form>
      </section>
    </div>
  );
}
