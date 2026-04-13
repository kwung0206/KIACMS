import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { fetchManagedStudents } from "../../../api/mentorApi";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import { useAuth } from "../../../hooks/useAuth";

export default function MentorHomePage() {
  const { user } = useAuth();
  const [managedStudents, setManagedStudents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const response = await fetchManagedStudents();
        if (active) {
          setManagedStudents(response);
        }
      } catch (error) {
        if (active) {
          setManagedStudents([]);
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

  const totalCourseMappings = useMemo(
    () =>
      managedStudents.reduce((sum, student) => sum + (student.enrolledCourses?.length || 0), 0),
    [managedStudents],
  );

  if (loading) {
    return <LoadingScreen message="멘토 관리자 화면을 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title={`${user?.email?.split("@")[0] || "mentor"} 관리자 홈`}
        description="멘토는 프로젝트 참여자가 아니라 담당 학생을 배정하고 수업 매핑을 관리하는 운영 역할입니다."
        actions={
          <Link className="primary-button button-small" to="/mentor/students">
            학생 관리 열기
          </Link>
        }
      />

      <section className="stats-grid">
        <article className="panel stat-card">
          <span>담당 학생</span>
          <strong>{managedStudents.length}</strong>
          <p>현재 내가 관리 중인 학생 수입니다.</p>
        </article>
        <article className="panel stat-card">
          <span>학생-수업 매핑</span>
          <strong>{totalCourseMappings}</strong>
          <p>학생 캘린더에 반영되는 활성 수업 매핑 수입니다.</p>
        </article>
        <article className="panel stat-card">
          <span>권한</span>
          <strong>MENTOR</strong>
          <p>프로젝트 지원이나 알림 기능은 멘토 역할에서 제외됩니다.</p>
        </article>
      </section>

      <section className="grid-two">
        <article className="panel">
          <div className="section-title-row">
            <h2>담당 학생 미리보기</h2>
            <Link className="text-button" to="/mentor/students">
              전체 관리로 이동
            </Link>
          </div>

          {managedStudents.length === 0 ? (
            <EmptyState
              title="아직 담당 학생이 없습니다."
              description="학생 관리 화면에서 학생을 검색하고 담당자로 배정해 보세요."
            />
          ) : (
            <div className="list-stack">
              {managedStudents.slice(0, 5).map((student) => (
                <div key={student.mappingId} className="info-card">
                  <strong>{student.studentName}</strong>
                  <span>{student.studentEmail}</span>
                  <small>배정 수업 {student.enrolledCourses?.length || 0}건</small>
                </div>
              ))}
            </div>
          )}
        </article>

        <article className="panel">
          <h2>운영 원칙</h2>
          <div className="list-stack">
            <div className="info-card">
              <strong>학생 선택과 해제</strong>
              <span>멘토는 자신이 관리할 학생을 직접 배정하고 해제할 수 있습니다.</span>
            </div>
            <div className="info-card">
              <strong>학생-수업 매핑</strong>
              <span>선택한 학생에게 수업을 매핑하면 학생 캘린더에 자동 반영됩니다.</span>
            </div>
            <div className="info-card">
              <strong>프로젝트 기능 제외</strong>
              <span>멘토 역할에서는 프로젝트 참가, 지원, 알림 기능을 노출하지 않습니다.</span>
            </div>
          </div>
        </article>
      </section>
    </div>
  );
}
