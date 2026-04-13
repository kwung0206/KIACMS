import { useEffect, useMemo, useState } from "react";
import {
  assignCourseToManagedStudent,
  assignManagedStudent,
  dropCourseFromManagedStudent,
  endManagedStudent,
  fetchManagedStudents,
  fetchMentorCourses,
  searchMentorStudents,
} from "../../../api/mentorApi";
import EmptyState from "../../../components/common/EmptyState";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";

function todayString() {
  return new Date().toISOString().slice(0, 10);
}

export default function MentorStudentsPage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const [studentOptions, setStudentOptions] = useState([]);
  const [managedStudents, setManagedStudents] = useState([]);
  const [courses, setCourses] = useState([]);
  const [selectedStudentId, setSelectedStudentId] = useState("");
  const [selectedMappingId, setSelectedMappingId] = useState("");
  const [assignmentMemo, setAssignmentMemo] = useState("");
  const [assignmentStartDate, setAssignmentStartDate] = useState(todayString());
  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ type: "", message: "" });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    const [studentsResponse, managedResponse, courseResponse] = await Promise.all([
      searchMentorStudents(""),
      fetchManagedStudents(),
      fetchMentorCourses(),
    ]);

    setStudentOptions(studentsResponse);
    setManagedStudents(managedResponse);
    setCourses(courseResponse);
    setSelectedStudentId((current) => current || studentsResponse[0]?.id || "");
    setSelectedMappingId((current) => current || managedResponse[0]?.mappingId || "");
    setSelectedCourseId((current) => current || courseResponse[0]?.id || "");
    setLoading(false);
  }

  async function handleSearch(event) {
    event.preventDefault();
    const response = await searchMentorStudents(searchKeyword);
    setStudentOptions(response);
    if (response.length > 0) {
      setSelectedStudentId(response[0].id);
    }
  }

  async function handleAssignStudent() {
    if (!selectedStudentId) {
      setFeedback({ type: "error", message: "배정할 학생을 먼저 선택해 주세요." });
      return;
    }

    try {
      await assignManagedStudent({
        studentId: selectedStudentId,
        memo: assignmentMemo || null,
        startDate: assignmentStartDate,
      });
      setFeedback({ type: "success", message: "학생이 담당 목록에 배정되었습니다." });
      setAssignmentMemo("");
      await loadData();
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    }
  }

  async function handleEndMapping(mappingId) {
    try {
      await endManagedStudent(mappingId);
      setFeedback({ type: "success", message: "학생 담당 배정을 해제했습니다." });
      await loadData();
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    }
  }

  async function handleAssignCourse() {
    if (!selectedMappingId || !selectedCourseId) {
      setFeedback({ type: "error", message: "학생과 수업을 모두 선택해 주세요." });
      return;
    }

    try {
      await assignCourseToManagedStudent(selectedMappingId, selectedCourseId);
      setFeedback({
        type: "success",
        message: "수업이 학생에게 매핑되었습니다. 학생 캘린더에 자동 반영됩니다.",
      });
      await loadData();
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    }
  }

  async function handleDropCourse(mappingId, courseId) {
    try {
      await dropCourseFromManagedStudent(mappingId, courseId);
      setFeedback({ type: "success", message: "학생과 수업의 매핑을 해제했습니다." });
      await loadData();
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    }
  }

  const selectedManagedStudent = useMemo(
    () => managedStudents.find((student) => student.mappingId === selectedMappingId) || null,
    [managedStudents, selectedMappingId],
  );
  const managedStudentIds = useMemo(
    () => new Set(managedStudents.map((student) => student.studentId)),
    [managedStudents],
  );

  if (loading) {
    return <LoadingScreen message="학생 관리 화면을 준비하고 있습니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="학생 관리"
        description="학생 검색, 담당자 배정, 학생-수업 매핑을 한 화면에서 처리합니다."
      />

      {feedback.message ? (
        <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
          {feedback.message}
        </div>
      ) : null}

      <section className="detail-grid">
        <article className="panel">
          <div className="section-title-row">
            <h2>학생 검색 및 배정</h2>
          </div>

          <form className="form-grid compact-grid spaced-bottom" onSubmit={handleSearch}>
            <FormField label="학생 검색" hint="이름 또는 이메일로 검색할 수 있습니다.">
              <input value={searchKeyword} onChange={(event) => setSearchKeyword(event.target.value)} />
            </FormField>

            <div className="button-row align-end">
              <button className="ghost-button button-small" type="submit">
                검색
              </button>
            </div>
          </form>

          <div className="list-stack spaced-bottom">
            {studentOptions.map((student) => (
              <button
                key={student.id}
                type="button"
                className={selectedStudentId === student.id ? "select-card active" : "select-card"}
                onClick={() => setSelectedStudentId(student.id)}
              >
                <strong>{student.name}</strong>
                <span>{student.email}</span>
                <small>{managedStudentIds.has(student.id) ? "이미 담당 중" : "배정 가능"}</small>
              </button>
            ))}
          </div>

          {studentOptions.length === 0 ? (
            <EmptyState
              title="검색 결과가 없습니다."
              description="학생 이름이나 이메일을 바꿔 다시 검색해 보세요."
            />
          ) : (
            <div className="panel nested-panel form-stack">
              <FormField label="배정 시작일">
                <input
                  type="date"
                  value={assignmentStartDate}
                  onChange={(event) => setAssignmentStartDate(event.target.value)}
                />
              </FormField>

              <FormField label="메모" hint="선택 입력입니다. 멘토 내부 메모 용도로 사용합니다.">
                <textarea
                  rows={4}
                  value={assignmentMemo}
                  onChange={(event) => setAssignmentMemo(event.target.value)}
                />
              </FormField>

              <div className="button-row">
                <button
                  className="primary-button button-small"
                  type="button"
                  onClick={handleAssignStudent}
                  disabled={!selectedStudentId || managedStudentIds.has(selectedStudentId)}
                >
                  담당 학생으로 배정
                </button>
              </div>
            </div>
          )}
        </article>

        <article className="panel">
          <div className="section-title-row">
            <h2>담당 학생 목록</h2>
          </div>

          {managedStudents.length === 0 ? (
            <EmptyState
              title="아직 담당 학생이 없습니다."
              description="왼쪽 패널에서 학생을 선택하고 담당자로 배정해 주세요."
            />
          ) : (
            <div className="list-stack">
              {managedStudents.map((student) => (
                <button
                  key={student.mappingId}
                  type="button"
                  className={selectedMappingId === student.mappingId ? "select-card active" : "select-card"}
                  onClick={() => setSelectedMappingId(student.mappingId)}
                >
                  <strong>{student.studentName}</strong>
                  <span>{student.studentEmail}</span>
                  <small>배정 수업 {student.enrolledCourses?.length || 0}건</small>
                </button>
              ))}
            </div>
          )}
        </article>
      </section>

      <section className="detail-grid">
        <article className="panel">
          <div className="section-title-row">
            <h2>학생-수업 매핑</h2>
          </div>

          {!selectedManagedStudent ? (
            <EmptyState
              title="선택된 담당 학생이 없습니다."
              description="오른쪽 목록에서 관리할 학생을 선택해 주세요."
            />
          ) : (
            <div className="form-stack">
              <div className="info-card">
                <strong>{selectedManagedStudent.studentName}</strong>
                <span>{selectedManagedStudent.studentEmail}</span>
                <small>배정 시작일 {selectedManagedStudent.startDate}</small>
              </div>

              <FormField label="매핑할 수업">
                <select value={selectedCourseId} onChange={(event) => setSelectedCourseId(event.target.value)}>
                  {courses.map((course) => (
                    <option key={course.id} value={course.id}>
                      {course.title} ({course.courseCode})
                    </option>
                  ))}
                </select>
              </FormField>

              <div className="button-row">
                <button className="primary-button button-small" type="button" onClick={handleAssignCourse}>
                  학생에게 수업 매핑
                </button>
                <button
                  className="danger-button button-small"
                  type="button"
                  onClick={() => handleEndMapping(selectedManagedStudent.mappingId)}
                >
                  학생 담당 해제
                </button>
              </div>

              <p className="muted-text">
                수업을 매핑하면 해당 과정의 회차 일정이 학생 캘린더에 자동으로 표시됩니다.
              </p>
            </div>
          )}
        </article>

        <article className="panel">
          <div className="section-title-row">
            <h2>현재 매핑된 수업</h2>
          </div>

          {!selectedManagedStudent || selectedManagedStudent.enrolledCourses.length === 0 ? (
            <EmptyState
              title="매핑된 수업이 없습니다."
              description="왼쪽에서 수업을 선택해 학생에게 먼저 매핑해 주세요."
            />
          ) : (
            <div className="list-stack">
              {selectedManagedStudent.enrolledCourses.map((course) => (
                <div key={course.enrollmentId} className="info-card">
                  <div className="spread-row">
                    <div>
                      <strong>{course.courseTitle}</strong>
                      <span>{course.courseCode}</span>
                    </div>
                    <button
                      className="danger-button button-small"
                      type="button"
                      onClick={() => handleDropCourse(selectedManagedStudent.mappingId, course.courseId)}
                    >
                      매핑 해제
                    </button>
                  </div>
                  <small>
                    과정 기간 {course.startDate} ~ {course.endDate}
                  </small>
                </div>
              ))}
            </div>
          )}
        </article>
      </section>
    </div>
  );
}
