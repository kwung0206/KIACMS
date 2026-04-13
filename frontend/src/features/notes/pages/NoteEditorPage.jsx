import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
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
import { addDays, formatDate, formatDateTime, getDateKey, todayRange } from "../../../utils/date";

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
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState(initialForm);
  const [sessionHints, setSessionHints] = useState([]);
  const [existingTags, setExistingTags] = useState([]);
  const [loading, setLoading] = useState(mode === "edit");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const selectedDate = searchParams.get("date") || "";
  const prefilledCourseId = searchParams.get("courseId") || "";
  const prefilledSessionId = searchParams.get("courseSessionId") || "";

  useEffect(() => {
    loadSessionHints();
  }, []);

  useEffect(() => {
    if (mode === "edit" && noteId) {
      loadNote();
    }
  }, [mode, noteId]);

  useEffect(() => {
    if (mode !== "create") {
      return;
    }

    setForm((current) => ({
      ...current,
      courseId: current.courseId || prefilledCourseId,
      courseSessionId: current.courseSessionId || prefilledSessionId,
    }));
  }, [mode, prefilledCourseId, prefilledSessionId]);

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

  const selectedSessionHint = useMemo(() => {
    if (!prefilledSessionId) {
      return null;
    }
    return sessionHints.find((event) => String(event.id) === prefilledSessionId) || null;
  }, [prefilledSessionId, sessionHints]);

  if (loading) {
    return <LoadingScreen message="노트 편집 화면을 준비하는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={mode === "edit" ? "노트 수정" : "노트 작성"}
        description="제목과 내용을 입력하고 필요하면 과정, 회차, 강사 태그를 함께 연결할 수 있습니다."
        actions={
          <Link className="ghost-button button-small" to="/student/notes">
            노트 목록으로
          </Link>
        }
      />

      {mode === "create" && (selectedDate || selectedSessionHint) ? (
        <section className="panel info-card">
          <strong>선택한 작성 기준</strong>
          <span>
            날짜: {selectedDate ? formatDate(selectedDate) : "미지정"}
            {selectedSessionHint ? ` · 수업: ${selectedSessionHint.sessionTitle}` : ""}
          </span>
          <small>
            {selectedSessionHint
              ? "선택한 수업의 과정 ID와 회차 ID가 자동으로 채워졌습니다."
              : "선택한 날짜 기준으로 노트를 작성합니다. 과정과 회차는 필요에 따라 직접 입력해 주세요."}
          </small>
        </section>
      ) : null}

      <form className="panel form-stack" onSubmit={handleSubmit}>
        <FormField label="제목">
          <input name="title" value={form.title} onChange={handleChange} required />
        </FormField>

        <FormField label="내용">
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
            label="과정 ID"
            hint="과정 선택 API가 아직 없어 UUID 직접 입력 방식으로 연결합니다."
          >
            <input name="courseId" value={form.courseId} onChange={handleChange} required />
          </FormField>

          <FormField label="회차 ID" hint="선택 입력입니다. 특정 회차와 연결할 때만 입력해 주세요.">
            <input
              name="courseSessionId"
              value={form.courseSessionId}
              onChange={handleChange}
            />
          </FormField>
        </div>

        <div className="info-card">
          <strong>최근 수업 일정</strong>
          {sessionHints.length === 0 ? (
            <p className="muted-text">최근 수업 안내를 불러오지 못했습니다.</p>
          ) : (
            <div className="list-stack compact-list">
              {sessionHints.slice(0, 6).map((event) => (
                <button
                  key={event.id}
                  type="button"
                  className={
                    String(event.id) === form.courseSessionId ? "select-card active" : "select-card"
                  }
                  onClick={() =>
                    setForm((current) => ({
                      ...current,
                      courseId: String(event.courseId),
                      courseSessionId: String(event.id),
                    }))
                  }
                >
                  <strong>{event.sessionTitle}</strong>
                  <span>{event.courseTitle}</span>
                  <small>{formatDateTime(event.start)}</small>
                </button>
              ))}
            </div>
          )}
        </div>

        {mode === "create" ? (
          <FormField
            label="태그할 강사 UUID 목록"
            hint="여러 명을 입력할 때는 쉼표로 구분해 주세요."
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
              <strong>현재 태그된 강사</strong>
              {existingTags.length === 0 ? (
                <p className="muted-text">아직 태그된 강사가 없습니다.</p>
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
              label="추가할 강사 UUID 목록"
              hint="수정 화면에서는 새로운 강사 태그 추가만 지원합니다."
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
            {saving ? "저장 중..." : mode === "edit" ? "노트 저장" : "노트 작성"}
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
