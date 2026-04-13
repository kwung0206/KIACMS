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
      setMessage("Comment saved.");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="Loading tagged note." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={note.title}
        description={`${note.authorName} | ${note.courseTitle}`}
        actions={
          <Link className="ghost-button button-small" to="/instructor/tagged-notes">
            Back to list
          </Link>
        }
      />

      <section className="detail-grid">
        <article className="panel">
          <h2>Student note</h2>
          <div className="info-card">
            <strong>Session info</strong>
            <span>{note.sessionTitle || "No session connected"}</span>
            {note.sessionInstructorName ? (
              <small>Instructor: {note.sessionInstructorName}</small>
            ) : null}
          </div>
          <pre className="note-content">{note.content}</pre>
        </article>

        <article className="panel">
          <h2>Tagged instructors</h2>
          {note.tags.length === 0 ? (
            <EmptyState
              title="No tags found."
              description="This note currently has no instructor tag records."
            />
          ) : (
            <div className="list-stack">
              {note.tags.map((tag) => (
                <div key={tag.id} className="info-card">
                  <strong>{tag.instructorName}</strong>
                  <span>{tag.instructorId}</span>
                  <small>
                    Tagged by {tag.taggedByName} at {formatDateTime(tag.taggedAt)}
                  </small>
                </div>
              ))}
            </div>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="section-title-row">
          <h2>Comment timeline</h2>
        </div>

        {note.comments.length === 0 ? (
          <p className="muted-text">No instructor comments yet.</p>
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
        <h2>Write comment</h2>
        <form className="form-stack" onSubmit={handleSubmit}>
          <FormField
            label="Feedback"
            hint="Leave a clear comment for the student. The message is saved as an instructor comment."
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
            {saving ? "Saving..." : "Save comment"}
          </button>
        </form>
      </section>
    </div>
  );
}
