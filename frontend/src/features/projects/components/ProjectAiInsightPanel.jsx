import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { requestProjectPlanInsights } from "../../../api/aiApi";
import FormField from "../../../components/common/FormField";

function buildInitialPlan(post) {
  if (!post) {
    return "";
  }

  return [
    `제목: ${post.title}`,
    `프로젝트 소개: ${post.description}`,
    `목표: ${post.goal}`,
    `기술 스택: ${post.techStack}`,
    `PM 소개: ${post.pmIntroduction}`,
    `PM 경험 및 강점: ${post.pmBackground}`,
  ].join("\n");
}

export default function ProjectAiInsightPanel({ post, visible }) {
  const [planText, setPlanText] = useState(() => buildInitialPlan(post));
  const [result, setResult] = useState(null);
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [loadingMode, setLoadingMode] = useState("");

  useEffect(() => {
    setPlanText(buildInitialPlan(post));
    setResult(null);
    setFeedback({ type: "", message: "" });
    setLoadingMode("");
  }, [post?.id]);

  const hasResult = Boolean(result);
  const canSendNotifications = useMemo(() => Boolean(post?.id), [post?.id]);

  async function handleAnalyze(sendNotifications) {
    setFeedback({ type: "", message: "" });
    setLoadingMode(sendNotifications ? "notify" : "analyze");

    try {
      const response = await requestProjectPlanInsights({
        projectPostId: post.id,
        projectPlanText: planText,
        sendNotifications,
        limit: 3,
      });

      setResult(response);
      setFeedback({
        type: "success",
        message: sendNotifications
          ? `추천 학생 ${response.notificationCount}명에게 알림을 보냈습니다.`
          : "AI 분석 결과를 불러왔습니다.",
      });
    } catch (error) {
      setFeedback({ type: "error", message: error.message });
    } finally {
      setLoadingMode("");
    }
  }

  if (!visible) {
    return null;
  }

  return (
    <section className="panel">
      <div className="section-title-row">
        <div>
          <h2>AI 프로젝트 분석</h2>
          <p className="muted-text">
            프로젝트 설명을 바탕으로 관련 강좌와 유사 프로젝트를 추천하고, 원하면 관련 수강생에게 추천 알림도 보낼 수 있습니다.
          </p>
        </div>
      </div>

      {feedback.message ? (
        <div className={feedback.type === "error" ? "form-alert error" : "form-alert"}>
          {feedback.message}
        </div>
      ) : null}

      <div className="form-stack">
        <FormField
          label="분석할 프로젝트 설명"
          hint="현재 모집글 내용을 기본으로 채워 두었습니다. 필요하면 계획서 메모를 덧붙여 더 구체적으로 분석할 수 있습니다."
        >
          <textarea rows={8} value={planText} onChange={(event) => setPlanText(event.target.value)} />
        </FormField>

        <div className="button-row">
          <button
            className="primary-button button-small"
            type="button"
            onClick={() => handleAnalyze(false)}
            disabled={loadingMode === "analyze" || loadingMode === "notify"}
          >
            {loadingMode === "analyze" ? "분석 중..." : "AI 분석 실행"}
          </button>
          <button
            className="ghost-button button-small"
            type="button"
            onClick={() => handleAnalyze(true)}
            disabled={!canSendNotifications || loadingMode === "analyze" || loadingMode === "notify"}
          >
            {loadingMode === "notify" ? "알림 전송 중..." : "추천 알림 보내기"}
          </button>
        </div>
      </div>

      {hasResult ? (
        <div className="page-stack compact-page-stack spaced-top">
          <div className="info-card">
            <strong>분석 요약</strong>
            <p>{result.analysisSummary}</p>
          </div>

          <div className="chatbot-section">
            <strong>핵심 키워드</strong>
            {result.keywords?.length ? (
              <div className="chatbot-chip-row">
                {result.keywords.map((keyword) => (
                  <span key={keyword} className="chatbot-chip">
                    {keyword}
                  </span>
                ))}
              </div>
            ) : (
              <p className="muted-text">추출된 키워드가 없습니다.</p>
            )}
          </div>

          <section className="grid-two">
            <article className="info-card">
              <strong>추천 강좌</strong>
              {result.recommendedCourses?.length ? (
                <div className="chatbot-card-stack spaced-top">
                  {result.recommendedCourses.map((course) => (
                    <div key={course.courseId} className="chatbot-recommendation-card">
                      <div className="spread-row">
                        <strong>{course.courseTitle}</strong>
                        <small>{course.courseCode}</small>
                      </div>
                      <span>{course.trackName || "트랙 미지정"}</span>
                      <p>{course.reason}</p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="muted-text spaced-top">추천할 강좌가 없습니다.</p>
              )}
            </article>

            <article className="info-card">
              <strong>유사 프로젝트</strong>
              {result.similarProjects?.length ? (
                <div className="chatbot-card-stack spaced-top">
                  {result.similarProjects.map((project) => (
                    <Link
                      key={project.projectPostId}
                      className="chatbot-recommendation-card chatbot-project-card"
                      to={`/projects/${project.projectPostId}`}
                    >
                      <div className="spread-row">
                        <strong>{project.title}</strong>
                        <small>{project.ownerName}</small>
                      </div>
                      <span>{project.recommendedPosition || "추천 포지션 안내 없음"}</span>
                      <p>{project.reason}</p>
                    </Link>
                  ))}
                </div>
              ) : (
                <p className="muted-text spaced-top">유사 프로젝트 추천이 없습니다.</p>
              )}
            </article>
          </section>

          <div className="info-card">
            <strong>알림 문구 초안</strong>
            <p>{result.notificationMessage || "생성된 알림 문구가 없습니다."}</p>
          </div>

          <div className="info-card">
            <div className="spread-row">
              <strong>추천 학생 미리보기</strong>
              <small>
                매칭 {result.matchedStudentCount}명 · 전송 {result.notificationCount}건
              </small>
            </div>
            {result.matchedStudents?.length ? (
              <div className="list-stack compact-list spaced-top">
                {result.matchedStudents.map((student) => (
                  <div key={student.studentId} className="hint-row">
                    <strong>{student.studentName}</strong>
                    <span>{student.matchedCourseTitle}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="muted-text spaced-top">
                아직 알림 전송 대상이 없습니다. 추천 알림 보내기를 실행하면 대상 학생 목록이 채워집니다.
              </p>
            )}
          </div>
        </div>
      ) : null}
    </section>
  );
}
