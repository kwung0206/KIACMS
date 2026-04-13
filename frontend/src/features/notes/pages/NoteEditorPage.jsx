import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { fetchStudentCalendar } from "../../../api/courseApi";
import {
  createNote,
  fetchMyNoteDetail,
  tagNoteInstructors,
  updateNote,
} from "../../../api/noteApi";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import { addDays, todayRange } from "../../../utils/date";

const initialForm = {
  title: "",
  content: "",
  courseId: "",
  courseSessionId: "",
  initialTagInstructorIds: "",
  additionalTagInstructorIds: "",
};

export default function NoteEditorPage({ mode }) {
  const navigate = useNavigate();
  const { noteId } = useParams();
  const [form, setForm] = useState(initialForm);
  const [sessionHints, setSessionHints] = useState([]);
  const [existingTags, setExistingTags] = useState([]);
  const [loading, setLoading] = useState(mode === "edit");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    loadSessionHints();
  }, []);

  useEffect(() => {
    if (mode === "edit" && noteId) {
      loadNote();
    }
  }, [mode, noteId]);

  async function loadSessionHints() {
    const today = todayRange().from;
    try {
      const response = await fetchStudentCalendar({
        from: addDays(today, -90),
        to: addDays(today, 120),
      });
      setSessionHints(response.events);
    } catch (error) {
      setSessionHints([]);
    }
  }

  async function loadNote() {
    try {
      const response = await fetchMyNoteDetail(noteId);
      setForm((current) => ({
        ...current,
        title: response.title,
        content: response.content,
        courseId: response.courseId,
        courseSessionId: response.courseSessionId || "",
        additionalTagInstructorIds: "",
      }));
      setExistingTags(response.tags);
    } finally {
      setLoading(false);
    }
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setMessage("");

    try {
      if (mode === "edit") {
        const updated = await updateNote(noteId, {
          title: form.title,
          content: form.content,
          courseId: form.courseId,
          courseSessionId: form.courseSessionId || null,
        });

        const additionalTagIds = parseUuidList(form.additionalTagInstructorIds);
        if (additionalTagIds.length > 0) {
          await tagNoteInstructors(updated.id, additionalTagIds);
        }

        navigate(`/student/notes/${updated.id}`, { replace: true });
        return;
      }

      const created = await createNote({
        title: form.title,
        content: form.content,
        courseId: form.courseId,
        courseSessionId: form.courseSessionId || null,
        taggedInstructorIds: parseUuidList(form.initialTagInstructorIds),
      });
      navigate(`/student/notes/${created.id}`, { replace: true });
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="Loading note editor." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={mode === "edit" ? "Edit note" : "Create note"}
        description="Add a title, content, course, and optional instructor tags. Instructor search is not available yet, so tags still use UUID input."
        actions={
          <Link className="ghost-button button-small" to="/student/notes">
            Back to notes
          </Link>
        }
      />

      <form className="panel form-stack" onSubmit={handleSubmit}>
        <FormField label="Title">
          <input name="title" value={form.title} onChange={handleChange} required />
        </FormField>

        <FormField label="Content">
          <textarea
            name="content"
            value={form.content}
            onChange={handleChange}
            rows={12}
            required
          />
        </FormField>

        <div className="form-grid">
          <FormField
            label="Course ID"
            hint="Current backend flow uses a direct UUID until a course picker API is added."
          >
            <input name="courseId" value={form.courseId} onChange={handleChange} required />
          </FormField>

          <FormField label="Session ID" hint="Optional. Connect the note to one class session when available.">
            <input
              name="courseSessionId"
              value={form.courseSessionId}
              onChange={handleChange}
            />
          </FormField>
        </div>

        <div className="info-card">
          <strong>Recent calendar sessions</strong>
          {sessionHints.length === 0 ? (
            <p className="muted-text">No recent session hints are available.</p>
          ) : (
            <div className="list-stack compact-list">
              {sessionHints.slice(0, 6).map((event) => (
                <div key={event.id} className="hint-row">
                  <span>{event.courseTitle}</span>
                  <small>
                    {event.title} | sessionId: {event.id}
                  </small>
                </div>
              ))}
            </div>
          )}
        </div>

        {mode === "create" ? (
          <FormField
            label="Tagged instructor UUID list"
            hint="Separate multiple instructor IDs with commas."
          >
            <input
              name="initialTagInstructorIds"
              value={form.initialTagInstructorIds}
              onChange={handleChange}
            />
          </FormField>
        ) : (
          <>
            <div className="info-card">
              <strong>Current tagged instructors</strong>
              {existingTags.length === 0 ? (
                <p className="muted-text">No tagged instructors yet.</p>
              ) : (
                <div className="list-stack compact-list">
                  {existingTags.map((tag) => (
                    <div key={tag.id} className="hint-row">
                      <span>{tag.instructorName}</span>
                      <small>{tag.instructorId}</small>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <FormField
              label="Additional tagged instructor UUID list"
              hint="Editing currently supports adding new instructor tags only."
            >
              <input
                name="additionalTagInstructorIds"
                value={form.additionalTagInstructorIds}
                onChange={handleChange}
              />
            </FormField>
          </>
        )}

        {message ? <div className="form-alert error">{message}</div> : null}

        <div className="button-row">
          <button className="primary-button button-small" type="submit" disabled={saving}>
            {saving ? "Saving..." : mode === "edit" ? "Save note" : "Create note"}
          </button>
        </div>
      </form>
    </div>
  );
}

function parseUuidList(value) {
  return value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}
