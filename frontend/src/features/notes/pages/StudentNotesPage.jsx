import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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
  getTodayDateKey,
} from "../../../utils/date";

export default function StudentNotesPage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({
    courseId: "",
    courseSessionId: "",
  });
  const [notes, setNotes] = useState([]);
  const [month, setMonth] = useState(() => new Date());
  const [selectedDate, setSelectedDate] = useState(() => getTodayDateKey());
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    loadNotes(filters);
  }, []);

  async function loadNotes(nextFilters) {
    setLoading(true);
    setErrorMessage("");

    try {
      const response = await fetchMyNotes(nextFilters);
      setNotes(response);
      if (response.length === 0) {
        setSelectedDate(getTodayDateKey());
      }
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
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

  const calendarNotes = useMemo(
    () =>
      notes.map((note) => ({
        ...note,
        date: getDateKey(note.createdAt),
        color: "#198038",
        label: note.title,
      })),
    [notes],
  );

  const selectedDayNotes = useMemo(
    () => calendarNotes.filter((note) => note.date === selectedDate),
    [calendarNotes, selectedDate],
  );

  return (
    <div className="page-stack">
      <PageHeader
        title="내 노트"
        description="목록과 월간 캘린더를 함께 보면서 날짜별 정리글 작성 내역을 확인할 수 있습니다."
        actions={
          <Link className="primary-button button-small" to="/student/notes/new">
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
          hint="특정 수업 회차만 보고 싶을 때 UUID를 입력해 주세요."
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
        events={calendarNotes}
        selectedDate={selectedDate}
        onMonthChange={(offset) => setMonth((current) => addMonths(current, offset))}
        onSelectDate={setSelectedDate}
        onEventClick={(note) => navigate(`/student/notes/${note.id}`)}
        emptyMessage="한국 시간 기준 오늘 날짜가 기본 선택됩니다. 노트가 없어도 캘린더는 항상 표시됩니다."
      />

      {loading ? (
        <LoadingScreen message="노트 목록을 불러오는 중입니다." />
      ) : selectedDayNotes.length === 0 ? (
        <EmptyState
          title={`${formatDate(selectedDate)}에 작성된 노트가 없습니다.`}
          description="다른 날짜를 선택하거나 새 노트를 작성해 보세요."
          action={
            <Link className="primary-button button-small" to="/student/notes/new">
              새 노트 작성
            </Link>
          }
        />
      ) : (
        <div className="panel table-panel">
          <div className="section-title-row">
            <h2>{formatDate(selectedDate)}</h2>
            <span className="muted-text">{selectedDayNotes.length}건</span>
          </div>

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
        </div>
      )}
    </div>
  );
}
