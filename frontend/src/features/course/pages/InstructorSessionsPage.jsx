import { useEffect, useMemo, useState } from "react";
import {
  fetchInstructorSessions,
  updateInstructorSessionResource,
} from "../../../api/courseApi";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDate, formatTime } from "../../../utils/date";

function fromResource(resource) {
  return {
    zoomLink: resource?.zoomLink || "",
    recordingLink: resource?.recordingLink || "",
    summaryLink: resource?.summaryLink || "",
    additionalNotice: resource?.additionalNotice || "",
  };
}

export default function InstructorSessionsPage() {
  const [sessions, setSessions] = useState([]);
  const [selectedId, setSelectedId] = useState("");
  const [draft, setDraft] = useState({
    zoomLink: "",
    recordingLink: "",
    summaryLink: "",
    additionalNotice: "",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const response = await fetchInstructorSessions();
        if (!active) {
          return;
        }

        setSessions(response);
        if (response.length > 0) {
          setSelectedId(response[0].id);
          setDraft(fromResource(response[0].resource));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      active = false;
    };
  }, []);

  const selectedSession = useMemo(
    () => sessions.find((session) => session.id === selectedId) || null,
    [sessions, selectedId],
  );

  function handleSelect(session) {
    setSelectedId(session.id);
    setDraft(fromResource(session.resource));
    setMessage("");
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setDraft((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (!selectedSession) {
      return;
    }

    setSaving(true);
    setMessage("");
    try {
      const updated = await updateInstructorSessionResource(selectedSession.id, draft);
      setSessions((current) =>
        current.map((item) => (item.id === updated.id ? updated : item)),
      );
      setDraft(fromResource(updated.resource));
      setMessage("Session resources saved.");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="Loading assigned sessions." />;
  }

  if (sessions.length === 0) {
    return (
      <EmptyState
        title="No assigned sessions yet."
        description="When root creates and assigns sessions, resource management becomes available here."
      />
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="Instructor session management"
        description="Register and update Zoom, recording, and summary links for each assigned session."
      />

      <div className="detail-grid">
        <aside className="panel">
          <h2>Assigned sessions</h2>
          <div className="list-stack">
            {sessions.map((session) => (
              <button
                key={session.id}
                type="button"
                className={selectedId === session.id ? "select-card active" : "select-card"}
                onClick={() => handleSelect(session)}
              >
                <div className="spread-row">
                  <strong>{session.courseTitle}</strong>
                  <StatusBadge value={session.status} />
                </div>
                <span>{session.title}</span>
                <small>
                  {formatDate(session.sessionDate)} {formatTime(session.startTime)} -{" "}
                  {formatTime(session.endTime)}
                </small>
              </button>
            ))}
          </div>
        </aside>

        <form className="panel" onSubmit={handleSubmit}>
          <div className="spread-row">
            <div>
              <h2>{selectedSession?.title}</h2>
              <p className="muted-text">
                {selectedSession?.courseTitle} ({selectedSession?.courseCode})
              </p>
            </div>
            <StatusBadge value={selectedSession?.status} />
          </div>

          <div className="form-stack">
            <FormField label="Zoom link" hint="Shown to students before class starts.">
              <input name="zoomLink" value={draft.zoomLink} onChange={handleChange} />
            </FormField>

            <FormField label="Recording link" hint="Shown after class ends.">
              <input name="recordingLink" value={draft.recordingLink} onChange={handleChange} />
            </FormField>

            <FormField label="Summary link" hint="Lecture summary or review material link.">
              <input name="summaryLink" value={draft.summaryLink} onChange={handleChange} />
            </FormField>

            <FormField label="Additional notice" hint="Visible to enrolled students.">
              <textarea
                name="additionalNotice"
                value={draft.additionalNotice}
                onChange={handleChange}
                rows={6}
              />
            </FormField>
          </div>

          {message ? <div className="form-alert">{message}</div> : null}

          <button className="primary-button button-small" type="submit" disabled={saving}>
            {saving ? "Saving..." : "Save resources"}
          </button>
        </form>
      </div>
    </div>
  );
}
