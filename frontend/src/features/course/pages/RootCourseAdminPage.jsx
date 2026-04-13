import { useEffect, useMemo, useState } from "react";
import {
  createCourse,
  createCourseSession,
  deleteCourseSession,
  fetchRootCourses,
  fetchRootInstructors,
  fetchRootSessionCalendar,
  updateCourseSession,
} from "../../../api/adminApi";
import MonthCalendar from "../../../components/calendar/MonthCalendar";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import {
  addMonths,
  formatDate,
  formatTime,
  getDateKey,
  getMonthRange,
  getTodayDateKey,
} from "../../../utils/date";
import { getStatusLabel } from "../../../utils/userLabels";

const COURSE_STATUS_OPTIONS = ["PLANNED", "IN_PROGRESS", "COMPLETED", "ARCHIVED"];
const SESSION_STATUS_OPTIONS = ["SCHEDULED", "COMPLETED", "CANCELLED"];

const initialCourseForm = {
  courseCode: "",
  title: "",
  description: "",
  trackName: "",
  startDate: "",
  endDate: "",
  maxCapacity: "",
  status: "PLANNED",
};

const initialSessionForm = {
  courseId: "",
  sessionOrder: 1,
  title: "",
  description: "",
  classroom: "",
  sessionDate: "",
  startTime: "09:00",
  endTime: "12:00",
  instructorId: "",
  status: "SCHEDULED",
};

function getCourseColor(seed = "") {
  const palette = ["#0f62fe", "#198038", "#8a3ffc", "#ba4e00", "#009d9a", "#d02670"];
  const value = seed.split("").reduce((sum, letter) => sum + letter.charCodeAt(0), 0);
  return palette[value % palette.length];
}

function buildCreateSessionForm(dateKey, courses, instructors) {
  return {
    ...initialSessionForm,
    sessionDate: dateKey,
    courseId: courses[0]?.id || "",
    instructorId: instructors[0]?.id || "",
  };
}

function buildEditSessionForm(event) {
  return {
    courseId: event.courseId,
    sessionOrder: event.sessionOrder,
    title: event.sessionTitle,
    description: event.description || "",
    classroom: event.classroom || "",
    sessionDate: getDateKey(event.start),
    startTime: event.start.slice(11, 16),
    endTime: event.end.slice(11, 16),
    instructorId: event.instructorId,
    status: event.status,
  };
}

function SessionEditorModal({
  isOpen,
  mode,
  sessionForm,
  setSessionForm,
  courses,
  instructors,
  selectedDate,
  saving,
  onClose,
  onSubmit,
  onDelete,
}) {
  if (!isOpen) {
    return null;
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setSessionForm((current) => ({ ...current, [name]: value }));
  }

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="modal-dialog"
        role="dialog"
        aria-modal="true"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div>
            <h2>{mode === "edit" ? "수업 일정 수정" : "수업 일정 등록"}</h2>
            <p className="muted-text">선택 날짜: {formatDate(selectedDate)}</p>
          </div>
          <button className="ghost-button button-small" type="button" onClick={onClose}>
            닫기
          </button>
        </div>

        <form className="form-stack" onSubmit={onSubmit}>
          <div className="form-grid">
            <FormField label="과정">
              <select name="courseId" value={sessionForm.courseId} onChange={handleChange} required>
                <option value="">과정을 선택해 주세요.</option>
                {courses.map((course) => (
                  <option key={course.id} value={course.id}>
                    {course.title} ({course.courseCode})
                  </option>
                ))}
              </select>
            </FormField>

            <FormField label="강사 이름" hint="승인된 강사만 표시됩니다.">
              <select
                name="instructorId"
                value={sessionForm.instructorId}
                onChange={handleChange}
                required
              >
                <option value="">강사를 선택해 주세요.</option>
                {instructors.map((instructor) => (
                  <option key={instructor.id} value={instructor.id}>
                    {instructor.name}
                  </option>
                ))}
              </select>
            </FormField>

            <FormField label="강의 제목">
              <input name="title" value={sessionForm.title} onChange={handleChange} required />
            </FormField>

            <FormField label="강의실">
              <input
                name="classroom"
                value={sessionForm.classroom}
                onChange={handleChange}
                required
              />
            </FormField>

            <FormField label="날짜">
              <input
                type="date"
                name="sessionDate"
                value={sessionForm.sessionDate}
                onChange={handleChange}
                required
              />
            </FormField>

            <FormField label="회차 번호">
              <input
                type="number"
                min="1"
                name="sessionOrder"
                value={sessionForm.sessionOrder}
                onChange={handleChange}
                required
              />
            </FormField>

            <FormField label="시작 시간">
              <input
                type="time"
                name="startTime"
                value={sessionForm.startTime}
                onChange={handleChange}
                required
              />
            </FormField>

            <FormField label="종료 시간">
              <input
                type="time"
                name="endTime"
                value={sessionForm.endTime}
                onChange={handleChange}
                required
              />
            </FormField>

            <FormField label="상태">
              <select name="status" value={sessionForm.status} onChange={handleChange}>
                {SESSION_STATUS_OPTIONS.map((status) => (
                  <option key={status} value={status}>
                    {getStatusLabel(status)}
                  </option>
                ))}
              </select>
            </FormField>
          </div>

          <FormField label="상세 설명" hint="수업 상세 화면에 표시할 메모나 안내를 입력할 수 있습니다.">
            <textarea
              rows={4}
              name="description"
              value={sessionForm.description}
              onChange={handleChange}
            />
          </FormField>

          <div className="button-row">
            <button className="primary-button button-small" type="submit" disabled={saving}>
              {saving ? "저장 중..." : mode === "edit" ? "수정 저장" : "수업 등록"}
            </button>
            {mode === "edit" ? (
              <button
                className="danger-button button-small"
                type="button"
                onClick={onDelete}
                disabled={saving}
              >
                일정 삭제
              </button>
            ) : null}
          </div>
        </form>
      </div>
    </div>
  );
}

export default function RootCourseAdminPage() {
  const [month, setMonth] = useState(() => new Date());
  const [selectedDate, setSelectedDate] = useState(() => getTodayDateKey());
  const [courses, setCourses] = useState([]);
  const [instructors, setInstructors] = useState([]);
  const [calendar, setCalendar] = useState({ timezone: "Asia/Seoul", events: [] });
  const [courseForm, setCourseForm] = useState(initialCourseForm);
  const [sessionForm, setSessionForm] = useState(initialSessionForm);
  const [editorMode, setEditorMode] = useState("create");
  const [editingSessionId, setEditingSessionId] = useState("");
  const [isEditorOpen, setIsEditorOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [savingCourse, setSavingCourse] = useState(false);
  const [savingSession, setSavingSession] = useState(false);
  const [feedback, setFeedback] = useState({ type: "", message: "" });

  useEffect(() => {
    loadStaticData();
  }, []);

  useEffect(() => {
    loadCalendar(month);
  }, [month]);

  async function loadStaticData() {
    setLoading(true);

    try {
      const [courseResponse, instructorResponse] = await Promise.all([
        fetchRootCourses(),
        fetchRootInstructors(),
      ]);

      setCourses(courseResponse);
      setInstructors(instructorResponse);
      setCourseForm((current) => ({
        ...current,
        startDate: current.startDate || getMonthRange(new Date()).from,
        endDate: current.endDate || getMonthRange(new Date()).to,
      }));
      setSessionForm((current) => ({
        ...current,
        courseId: current.courseId || courseResponse[0]?.id || "",
        instructorId: current.instructorId || instructorResponse[0]?.id || "",
      }));
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function loadCalendar(targetMonth) {
    try {
      const response = await fetchRootSessionCalendar(getMonthRange(targetMonth));
      setCalendar(response);
    } catch (error) {
      setCalendar({ timezone: "Asia/Seoul", events: [] });
      setFeedback({ type: "error", message: error.message });
    }
  }

  function handleCourseChange(event) {
    const { name, value } = event.target;
    setCourseForm((current) => ({ ...current, [name]: value }));
  }

  async function handleCreateCourse(event) {
    event.preventDefault();
    setSavingCourse(true);
    setFeedback({ type: "", message: "" });

    try {
      await createCourse({
        ...courseForm,
        maxCapacity: courseForm.maxCapacity ? Number(courseForm.maxCapacity) : null,
      });
      setFeedback({ type: "success", message: "과정이 생성되었습니다." });
      setCourseForm({
        ...initialCourseForm,
        startDate: getMonthRange(new Date()).from,
        endDate: getMonthRange(new Date()).to,
      });
      await loadStaticData();
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    } finally {
      setSavingCourse(false);
    }
  }

  function openCreateEditor(dateKey) {
    setSelectedDate(dateKey);
    setEditorMode("create");
    setEditingSessionId("");
    setSessionForm(buildCreateSessionForm(dateKey, courses, instructors));
    setIsEditorOpen(true);
  }

  function openEditEditor(event) {
    setSelectedDate(getDateKey(event.start));
    setEditorMode("edit");
    setEditingSessionId(event.id);
    setSessionForm(buildEditSessionForm(event));
    setIsEditorOpen(true);
  }

  async function handleSessionSubmit(event) {
    event.preventDefault();
    setSavingSession(true);
    setFeedback({ type: "", message: "" });

    const payload = {
      ...sessionForm,
      sessionOrder: Number(sessionForm.sessionOrder),
    };

    try {
      if (editorMode === "edit" && editingSessionId) {
        await updateCourseSession(editingSessionId, payload);
        setFeedback({ type: "success", message: "수업 일정이 수정되었습니다." });
      } else {
        await createCourseSession(sessionForm.courseId, payload);
        setFeedback({ type: "success", message: "수업 일정이 등록되었습니다." });
      }

      setIsEditorOpen(false);
      await loadCalendar(month);
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    } finally {
      setSavingSession(false);
    }
  }

  async function handleDeleteSession() {
    if (!editingSessionId) {
      return;
    }

    setSavingSession(true);
    setFeedback({ type: "", message: "" });

    try {
      await deleteCourseSession(editingSessionId);
      setFeedback({ type: "success", message: "수업 일정이 삭제되었습니다." });
      setIsEditorOpen(false);
      await loadCalendar(month);
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    } finally {
      setSavingSession(false);
    }
  }

  const calendarEvents = useMemo(
    () =>
      calendar.events.map((event) => ({
        ...event,
        date: getDateKey(event.start),
        color: getCourseColor(event.courseCode),
        label: event.sessionTitle,
      })),
    [calendar.events],
  );

  const selectedDayEvents = useMemo(
    () => calendarEvents.filter((event) => event.date === selectedDate),
    [calendarEvents, selectedDate],
  );

  if (loading) {
    return <LoadingScreen message="Root 수업 일정 관리 화면을 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="수업 일정 관리"
        description="캘린더 날짜를 직접 클릭해 수업을 등록하고, 과정과 회차를 Root 권한으로 관리할 수 있습니다."
      />

      {feedback.message ? (
        <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
          {feedback.message}
        </div>
      ) : null}

      <MonthCalendar
        month={month}
        events={calendarEvents}
        selectedDate={selectedDate}
        onMonthChange={(offset) => setMonth((current) => addMonths(current, offset))}
        onSelectDate={openCreateEditor}
        onEventClick={openEditEditor}
        emptyMessage="캘린더는 항상 표시됩니다. 날짜를 눌러 바로 수업 일정을 등록할 수 있습니다."
      />

      <section className="detail-grid">
        <article className="panel">
          <div className="section-title-row">
            <h2>{formatDate(selectedDate)} 일정</h2>
            <button
              className="primary-button button-small"
              type="button"
              onClick={() => openCreateEditor(selectedDate)}
            >
              이 날짜에 수업 등록
            </button>
          </div>

          {selectedDayEvents.length === 0 ? (
            <EmptyState
              title="선택한 날짜에 등록된 수업이 없습니다."
              description="달력은 그대로 유지되며, 바로 새 수업 일정을 등록할 수 있습니다."
            />
          ) : (
            <div className="list-stack">
              {selectedDayEvents.map((event) => (
                <button
                  key={event.id}
                  type="button"
                  className="select-card"
                  onClick={() => openEditEditor(event)}
                >
                  <div className="spread-row">
                    <div className="list-stack compact-list">
                      <strong>{event.sessionTitle}</strong>
                      <span>{event.courseTitle}</span>
                    </div>
                    <StatusBadge value={event.status} />
                  </div>
                  <div className="meta-grid">
                    <small>강의실: {event.classroom}</small>
                    <small>
                      시간: {formatTime(event.start)} - {formatTime(event.end)}
                    </small>
                    <small>강사 이름: {event.instructorName}</small>
                  </div>
                </button>
              ))}
            </div>
          )}
        </article>

        <article className="panel">
          <h2>과정 생성</h2>
          <form className="form-stack" onSubmit={handleCreateCourse}>
            <div className="form-grid">
              <FormField label="과정 코드">
                <input
                  name="courseCode"
                  value={courseForm.courseCode}
                  onChange={handleCourseChange}
                  required
                />
              </FormField>

              <FormField label="과정명">
                <input name="title" value={courseForm.title} onChange={handleCourseChange} required />
              </FormField>

              <FormField label="트랙명">
                <input name="trackName" value={courseForm.trackName} onChange={handleCourseChange} />
              </FormField>

              <FormField label="정원">
                <input
                  type="number"
                  min="1"
                  name="maxCapacity"
                  value={courseForm.maxCapacity}
                  onChange={handleCourseChange}
                />
              </FormField>

              <FormField label="시작일">
                <input
                  type="date"
                  name="startDate"
                  value={courseForm.startDate}
                  onChange={handleCourseChange}
                  required
                />
              </FormField>

              <FormField label="종료일">
                <input
                  type="date"
                  name="endDate"
                  value={courseForm.endDate}
                  onChange={handleCourseChange}
                  required
                />
              </FormField>

              <FormField label="상태">
                <select name="status" value={courseForm.status} onChange={handleCourseChange}>
                  {COURSE_STATUS_OPTIONS.map((status) => (
                    <option key={status} value={status}>
                      {getStatusLabel(status)}
                    </option>
                  ))}
                </select>
              </FormField>
            </div>

            <FormField label="과정 설명">
              <textarea
                rows={4}
                name="description"
                value={courseForm.description}
                onChange={handleCourseChange}
              />
            </FormField>

            <div className="button-row">
              <button className="primary-button button-small" type="submit" disabled={savingCourse}>
                {savingCourse ? "생성 중..." : "과정 생성"}
              </button>
            </div>
          </form>

          <div className="spaced-top">
            <h3>등록된 과정</h3>
            {courses.length === 0 ? (
              <p className="muted-text">회차를 등록하려면 먼저 최소 1개의 과정을 생성해 주세요.</p>
            ) : (
              <div className="list-stack compact-list">
                {courses.map((course) => (
                  <div key={course.id} className="hint-row">
                    <span>
                      {course.title} ({course.courseCode})
                    </span>
                    <small>
                      {course.startDate} ~ {course.endDate}
                    </small>
                  </div>
                ))}
              </div>
            )}
          </div>
        </article>
      </section>

      <SessionEditorModal
        isOpen={isEditorOpen}
        mode={editorMode}
        sessionForm={sessionForm}
        setSessionForm={setSessionForm}
        courses={courses}
        instructors={instructors}
        selectedDate={selectedDate}
        saving={savingSession}
        onClose={() => setIsEditorOpen(false)}
        onSubmit={handleSessionSubmit}
        onDelete={handleDeleteSession}
      />
    </div>
  );
}
