import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchStudentCalendar } from "../../../api/courseApi";
import MonthCalendar from "../../../components/calendar/MonthCalendar";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import {
  addMonths,
  formatDate,
  formatDateTime,
  getDateKey,
  getMonthRange,
  getTodayDateKey,
} from "../../../utils/date";

function getCourseColor(seed = "") {
  const palette = ["#0f62fe", "#198038", "#8a3ffc", "#ba4e00", "#009d9a", "#d02670"];
  const value = seed.split("").reduce((sum, letter) => sum + letter.charCodeAt(0), 0);
  return palette[value % palette.length];
}

function ResourcePill({ label, href }) {
  if (!href) {
    return <span className="resource-pill muted">{label}</span>;
  }

  return (
    <a className="resource-pill active" href={href} target="_blank" rel="noreferrer">
      {label}
    </a>
  );
}

function isDateKeyInMonth(dateKey, month) {
  const range = getMonthRange(month);
  return dateKey >= range.from && dateKey <= range.to;
}

function createMonthDate(dateKey) {
  const [year, month] = dateKey.split("-").map(Number);
  return new Date(year, month - 1, 1);
}

export default function StudentCalendarPage() {
  const navigate = useNavigate();
  const [month, setMonth] = useState(() => createMonthDate(getTodayDateKey()));
  const [calendar, setCalendar] = useState({ timezone: "Asia/Seoul", events: [] });
  const [selectedDate, setSelectedDate] = useState(() => getTodayDateKey());
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let active = true;

    async function loadCalendar() {
      setLoading(true);
      setErrorMessage("");

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
        setErrorMessage(error.message);
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadCalendar();
    return () => {
      active = false;
    };
  }, [month]);

  useEffect(() => {
    setSelectedDate((current) => {
      if (current && isDateKeyInMonth(current, month)) {
        return current;
      }

      const today = getTodayDateKey();
      if (isDateKeyInMonth(today, month)) {
        return today;
      }

      return getMonthRange(month).from;
    });
  }, [month]);

  const calendarEvents = useMemo(
    () =>
      calendar.events.map((event) => ({
        ...event,
        date: getDateKey(event.start),
        color: getCourseColor(event.courseCode),
        label: event.sessionTitle || event.courseCode,
      })),
    [calendar.events],
  );

  const selectedDayEvents = useMemo(
    () => calendarEvents.filter((event) => event.date === selectedDate),
    [calendarEvents, selectedDate],
  );

  function handleSelectDate(dateKey) {
    setSelectedDate(dateKey);
    if (!isDateKeyInMonth(dateKey, month)) {
      setMonth(createMonthDate(dateKey));
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="학생 수업 캘린더"
        description="일정이 없어도 월간 캘린더는 항상 보입니다. 한국 시간 기준 오늘 날짜에 맞춰 기본 선택이 표시됩니다."
      />

      {errorMessage ? <div className="form-alert error">{errorMessage}</div> : null}

      <MonthCalendar
        month={month}
        events={calendarEvents}
        selectedDate={selectedDate}
        onMonthChange={(offset) => setMonth((current) => addMonths(current, offset))}
        onSelectDate={handleSelectDate}
        onEventClick={(event) => navigate(`/student/sessions/${event.id}`)}
        emptyMessage="등록된 일정이 없어도 캘린더는 그대로 유지됩니다."
      />

      {loading ? (
        <LoadingScreen message="캘린더 일정을 불러오는 중입니다." />
      ) : selectedDayEvents.length === 0 ? (
        <EmptyState
          title={`${formatDate(selectedDate)} 일정이 없습니다.`}
          description="선택한 날짜에 등록된 수업이 없습니다. 다른 날짜를 눌러 일정을 확인해 보세요."
        />
      ) : (
        <section className="list-stack">
          {selectedDayEvents.map((event) => (
            <article key={event.id} className="panel info-card calendar-event-card">
              <div className="spread-row">
                <div className="list-stack compact-list">
                  <strong>{event.sessionTitle}</strong>
                  <span>{event.courseTitle}</span>
                </div>
                <span className="calendar-event-color" style={{ backgroundColor: event.color }} />
              </div>

              <div className="meta-grid">
                <small>일시: {formatDateTime(event.start)}</small>
                <small>강사: {event.instructorName}</small>
                <small>강의실: {event.classroom || "-"}</small>
              </div>

              <div className="resource-pill-row">
                <ResourcePill label="Zoom" href={event.zoomLink} />
                <ResourcePill label="녹화본" href={event.recordingLink} />
                <ResourcePill label="정리 링크" href={event.summaryLink} />
              </div>

              <div className="button-row">
                <button
                  className="ghost-button button-small"
                  type="button"
                  onClick={() => navigate(`/student/sessions/${event.id}`)}
                >
                  회차 상세 보기
                </button>
              </div>
            </article>
          ))}
        </section>
      )}
    </div>
  );
}
