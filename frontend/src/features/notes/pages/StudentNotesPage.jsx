import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { fetchStudentCalendar } from "../../../api/courseApi";
import { fetchMyNotes } from "../../../api/noteApi";
import MonthCalendar from "../../../components/calendar/MonthCalendar";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import {
  addMonths,
  formatDate,
  formatDateTime,
  getDateKey,
  getMonthRange,
  getTodayDateKey,
  isSameDateKey,
} from "../../../utils/date";

function getCourseColor(seed = "") {
  const palette = ["#0f62fe", "#198038", "#8a3ffc", "#ba4e00", "#009d9a", "#d02670"];
  const value = seed.split("").reduce((sum, letter) => sum + letter.charCodeAt(0), 0);
  return palette[value % palette.length];
}

function buildNoteCreatePath({ date, courseId, courseSessionId }) {
  const params = new URLSearchParams();
  if (date) {
    params.set("date", date);
  }
  if (courseId) {
    params.set("courseId", courseId);
  }
  if (courseSessionId) {
    params.set("courseSessionId", courseSessionId);
  }
  const query = params.toString();
  return query ? `/student/notes/new?${query}` : "/student/notes/new";
}

export default function StudentNotesPage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({
    courseId: "",
    courseSessionId: "",
  });
  const [notes, setNotes] = useState([]);
  const [calendar, setCalendar] = useState({ timezone: "Asia/Seoul", events: [] });
  const [month, setMonth] = useState(() => new Date());
  const [selectedDate, setSelectedDate] = useState(() => getTodayDateKey());
  const [loadingNotes, setLoadingNotes] = useState(true);
  const [loadingCalendar, setLoadingCalendar] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [sessionModalDate, setSessionModalDate] = useState("");

  useEffect(() => {
    loadNotes(filters);
  }, []);

  useEffect(() => {
    let active = true;

    async function loadCalendar() {
      setLoadingCalendar(true);

      try {
        const response = await fetchStudentCalendar(getMonthRange(month));
        if (!active) {
          return;
        }
        setCalendar(response);
      } catch (error) {
        if (!active) {
          return;
        }
        setCalendar({ timezone: "Asia/Seoul", events: [] });
      } finally {
        if (active) {
          setLoadingCalendar(false);
        }
      }
    }

    loadCalendar();
    return () => {
      active = false;
    };
  }, [month]);

  async function loadNotes(nextFilters) {
    setLoadingNotes(true);
    setErrorMessage("");

    try {
      const response = await fetchMyNotes(nextFilters);
      setNotes(response);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoadingNotes(false);
    }
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    await loadNotes(filters);
  }

  async function handleReset() {
    const resetFilters = { courseId: "", courseSessionId: "" };
    setFilters(resetFilters);
    await loadNotes(resetFilters);
  }

  const sessionEvents = useMemo(
    () =>
      calendar.events.map((event) => ({
        ...event,
        markerKey: `session-${event.id}`,
        date: getDateKey(event.start),
        color: getCourseColor(event.courseCode),
        label: event.sessionTitle || event.courseCode,
        eventType: "SESSION",
      })),
    [calendar.events],
  );

  const noteEvents = useMemo(
    () =>
      notes.map((note) => ({
        ...note,
        markerKey: `note-${note.id}`,
        date: getDateKey(note.createdAt),
        color: "#198038",
        label: note.title,
        eventType: "NOTE",
      })),
    [notes],
  );

  const calendarEvents = useMemo(() => [...sessionEvents, ...noteEvents], [sessionEvents, noteEvents]);

  const selectedDayNotes = useMemo(
    () => noteEvents.filter((note) => note.date === selectedDate),
    [noteEvents, selectedDate],
  );

  const selectedDaySessions = useMemo(
    () => sessionEvents.filter((event) => event.date === selectedDate),
    [sessionEvents, selectedDate],
  );

  const modalSessions = useMemo(
    () => sessionEvents.filter((event) => event.date === sessionModalDate),
    [sessionEvents, sessionModalDate],
  );

  function openNoteEditorForDate(dateKey) {
    const sessionsOnDate = sessionEvents.filter((event) => event.date === dateKey);
    setSelectedDate(dateKey);

    if (sessionsOnDate.length === 0) {
      navigate(buildNoteCreatePath({ date: dateKey }));
      return;
    }

    setSessionModalDate(dateKey);
  }

  function handleMonthChange(offset) {
    setMonth((current) => addMonths(current, offset));
  }

  function handleEventClick(event) {
    if (event.eventType === "NOTE") {
      navigate(`/student/notes/${event.id}`);
      return;
    }

    navigate(
      buildNoteCreatePath({
        date: event.date,
        courseId: event.courseId,
        courseSessionId: event.id,
      }),
    );
  }

  const isLoading = loadingNotes || loadingCalendar;

  return (
    <div className="page-stack">
      <PageHeader
        title="내 노트"
        description="노트 작성 이력과 수업 일정을 한 캘린더에서 함께 확인할 수 있습니다. 날짜를 누르면 수업 선택 후 바로 노트 작성으로 이동합니다."
        actions={
          <Link className="primary-button button-small" to={buildNoteCreatePath({ date: selectedDate })}>
            새 노트 작성
          </Link>
        }
      />

      <form className="panel form-grid" onSubmit={handleSubmit}>
        <FormField
          label="과정 ID"
          hint="과정 검색 API가 아직 없어 UUID 직접 입력 방식으로 필터링합니다."
        >
          <input name="courseId" value={filters.courseId} onChange={handleChange} />
        </FormField>

        <FormField
          label="회차 ID"
          hint="특정 회차 기준으로만 보고 싶을 때 UUID를 입력해 주세요."
        >
          <input name="courseSessionId" value={filters.courseSessionId} onChange={handleChange} />
        </FormField>

        <div className="button-row align-end">
          <button className="primary-button button-small" type="submit">
            조회
          </button>
          <button className="ghost-button button-small" type="button" onClick={handleReset}>
            초기화
          </button>
        </div>
      </form>

      {errorMessage ? <div className="form-alert error">{errorMessage}</div> : null}

      <MonthCalendar
        month={month}
        events={calendarEvents}
        selectedDate={selectedDate}
        onMonthChange={handleMonthChange}
        onSelectDate={openNoteEditorForDate}
        onEventClick={handleEventClick}
        emptyMessage="수업 일정은 파란색 계열, 작성한 노트는 초록색 계열로 표시됩니다. 날짜를 클릭하면 그 날짜 기준으로 노트 작성으로 이어집니다."
      />

      {isLoading ? (
        <LoadingScreen message="노트와 수업 일정을 불러오는 중입니다." />
      ) : selectedDayNotes.length === 0 && selectedDaySessions.length === 0 ? (
        <EmptyState
          title={`${formatDate(selectedDate)}에는 표시할 항목이 없습니다.`}
          description="날짜를 클릭하면 바로 노트 작성 페이지로 이동합니다."
          action={
            <Link className="primary-button button-small" to={buildNoteCreatePath({ date: selectedDate })}>
              이 날짜로 노트 작성
            </Link>
          }
        />
      ) : (
        <div className="detail-grid">
          <section className="panel table-panel">
            <div className="section-title-row">
              <h2>{formatDate(selectedDate)} 노트</h2>
              <span className="muted-text">{selectedDayNotes.length}건</span>
            </div>

            {selectedDayNotes.length === 0 ? (
              <EmptyState
                title="작성된 노트가 없습니다."
                description="위 캘린더에서 날짜를 눌러 새 노트를 작성해 보세요."
              />
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>제목</th>
                    <th>과정</th>
                    <th>회차</th>
                    <th>태그 수</th>
                    <th>코멘트 수</th>
                    <th>수정 시각</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedDayNotes.map((note) => (
                    <tr key={note.id}>
                      <td>
                        <Link to={`/student/notes/${note.id}`}>{note.title}</Link>
                      </td>
                      <td>{note.courseTitle}</td>
                      <td>{note.sessionTitle || "-"}</td>
                      <td>{note.tagCount}</td>
                      <td>{note.commentCount}</td>
                      <td>{formatDateTime(note.updatedAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>

          <section className="panel">
            <div className="section-title-row">
              <h2>{formatDate(selectedDate)} 수업 일정</h2>
              <span className="muted-text">{selectedDaySessions.length}개</span>
            </div>

            {selectedDaySessions.length === 0 ? (
              <EmptyState
                title="예정된 수업이 없습니다."
                description="이 날짜를 클릭하면 과정과 회차를 직접 입력하는 노트 작성 화면으로 이동합니다."
              />
            ) : (
              <div className="list-stack">
                {selectedDaySessions.map((event) => (
                  <button
                    key={event.id}
                    type="button"
                    className="select-card"
                    onClick={() =>
                      navigate(
                        buildNoteCreatePath({
                          date: selectedDate,
                          courseId: event.courseId,
                          courseSessionId: event.id,
                        }),
                      )
                    }
                  >
                    <strong>{event.sessionTitle}</strong>
                    <span>{event.courseTitle}</span>
                    <small>
                      {formatDateTime(event.start)} | 강사 {event.instructorName}
                    </small>
                  </button>
                ))}
              </div>
            )}
          </section>
        </div>
      )}

      {sessionModalDate ? (
        <div className="modal-backdrop" role="presentation" onClick={() => setSessionModalDate("")}>
          <div
            className="modal-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="note-session-select-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="modal-header">
              <div>
                <h2 id="note-session-select-title">수업 선택</h2>
                <p className="muted-text">
                  {formatDate(sessionModalDate)}에 등록된 수업입니다. 선택한 수업의 과정 ID와 회차 ID를 자동으로 채워서 노트 작성 화면으로 이동합니다.
                </p>
              </div>
            </div>

            <div className="list-stack spaced-bottom">
              {modalSessions.map((event) => (
                <button
                  key={event.id}
                  type="button"
                  className="select-card"
                  onClick={() =>
                    navigate(
                      buildNoteCreatePath({
                        date: sessionModalDate,
                        courseId: event.courseId,
                        courseSessionId: event.id,
                      }),
                    )
                  }
                >
                  <strong>{event.sessionTitle}</strong>
                  <span>{event.courseTitle}</span>
                  <small>
                    {formatDateTime(event.start)} | 강사 {event.instructorName} | 강의실 {event.classroom || "-"}
                  </small>
                </button>
              ))}
            </div>

            <div className="button-row align-end">
              <button className="ghost-button button-small" type="button" onClick={() => setSessionModalDate("")}>
                닫기
              </button>
              <button
                className="primary-button button-small"
                type="button"
                onClick={() => navigate(buildNoteCreatePath({ date: sessionModalDate }))}
              >
                수업 없이 작성
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
