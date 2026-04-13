import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { sendChatbotMessage } from "../../api/aiApi";
import FormField from "../common/FormField";
import { useAuth } from "../../hooks/useAuth";
import { getRoleLabel } from "../../utils/userLabels";

function buildInitialMessage(roleType, name) {
  const roleLabel = getRoleLabel(roleType);
  const greetingName = name || roleLabel;

  return {
    id: `assistant-welcome-${roleType || "user"}`,
    role: "assistant",
    content: `${greetingName}님을 위한 KIACMS AI 도우미입니다. 사이트 사용법, 역할별 메뉴, 진로 기반 강좌 추천, 프로젝트 계획서 분석까지 도와드릴게요.`,
    keywords: [],
    suggestedLinks: [],
    followUpQuestions: [],
    recommendedCourses: [],
    recommendedProjects: [],
  };
}

function getQuickPrompts(roleType) {
  switch (roleType) {
    case "ROOT":
      return [
        "관리자 승인은 어디서 하나요?",
        "수업 일정 관리는 어디서 하나요?",
        "프로젝트 삭제 관리는 어디서 하나요?",
      ];
    case "INSTRUCTOR":
      return [
        "태그된 정리글은 어디서 확인하나요?",
        "담당 회차는 어디서 관리하나요?",
        "저는 백엔드 개발자가 되고 싶은데 어떤 강좌가 좋을까요?",
      ];
    case "MENTOR":
      return [
        "멘토는 어떤 기능을 사용할 수 있나요?",
        "관리 학생은 어디서 배정하나요?",
        "학생에게 수업을 매핑하려면 어디로 가야 하나요?",
      ];
    case "STUDENT":
    default:
      return [
        "이 사이트에서 지원서는 어디서 수정하나요?",
        "내 모집글은 어디서 볼 수 있나요?",
        "저는 백엔드 개발자가 되고 싶은데 어떤 강좌를 들으면 좋을까요?",
        "프로젝트 계획서를 보고 관련 강좌를 추천해줄 수 있나요?",
      ];
  }
}

function toHistory(messages) {
  return messages
    .filter((message) => !message.loading && (message.role === "assistant" || message.role === "user"))
    .slice(-8)
    .map((message) => ({
      role: message.role,
      content: message.content,
    }));
}

function MessageBubble({ message, onFollowUp, onOpenLink, onOpenProject }) {
  return (
    <article className={`chatbot-message chatbot-message-${message.role}`}>
      <div className="chatbot-message-bubble">
        <strong className="chatbot-message-author">
          {message.role === "assistant" ? "KIACMS AI" : "나"}
        </strong>
        <p>{message.content}</p>

        {message.keywords?.length ? (
          <div className="chatbot-chip-row">
            {message.keywords.map((keyword) => (
              <span key={keyword} className="chatbot-chip">
                {keyword}
              </span>
            ))}
          </div>
        ) : null}

        {message.suggestedLinks?.length ? (
          <div className="chatbot-section">
            <strong>바로 가기</strong>
            <div className="chatbot-link-grid">
              {message.suggestedLinks.map((link) => (
                <button
                  key={`${link.url}-${link.label}`}
                  type="button"
                  className="ghost-button button-small chatbot-action-button"
                  onClick={() => onOpenLink(link)}
                >
                  {link.label}
                </button>
              ))}
            </div>
          </div>
        ) : null}

        {message.recommendedCourses?.length ? (
          <div className="chatbot-section">
            <strong>추천 강좌</strong>
            <div className="chatbot-card-stack">
              {message.recommendedCourses.map((course) => (
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
          </div>
        ) : null}

        {message.recommendedProjects?.length ? (
          <div className="chatbot-section">
            <strong>관련 프로젝트</strong>
            <div className="chatbot-card-stack">
              {message.recommendedProjects.map((project) => (
                <button
                  key={project.projectPostId}
                  type="button"
                  className="chatbot-recommendation-card chatbot-project-card"
                  onClick={() => onOpenProject(project.projectPostId)}
                >
                  <div className="spread-row">
                    <strong>{project.title}</strong>
                    <small>{project.ownerName}</small>
                  </div>
                  <span>{project.recommendedPosition || "추천 포지션 안내 없음"}</span>
                  <p>{project.reason}</p>
                </button>
              ))}
            </div>
          </div>
        ) : null}

        {message.followUpQuestions?.length ? (
          <div className="chatbot-section">
            <strong>이어서 물어보기</strong>
            <div className="chatbot-link-grid">
              {message.followUpQuestions.map((question) => (
                <button
                  key={question}
                  type="button"
                  className="ghost-button button-small chatbot-action-button"
                  onClick={() => onFollowUp(question)}
                >
                  {question}
                </button>
              ))}
            </div>
          </div>
        ) : null}
      </div>
    </article>
  );
}

export default function ChatbotDrawer({ open, currentPath, onClose }) {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [messages, setMessages] = useState(() => [buildInitialMessage(user?.roleType, user?.name)]);
  const [draft, setDraft] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const scrollContainerRef = useRef(null);

  const quickPrompts = useMemo(() => getQuickPrompts(user?.roleType), [user?.roleType]);

  useEffect(() => {
    setMessages([buildInitialMessage(user?.roleType, user?.name)]);
    setDraft("");
    setError("");
    setLoading(false);
  }, [user?.id, user?.name, user?.roleType]);

  useEffect(() => {
    if (!open) {
      return undefined;
    }

    function handleKeyDown(event) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose, open]);

  useEffect(() => {
    if (!open || !scrollContainerRef.current) {
      return;
    }

    const node = scrollContainerRef.current;
    node.scrollTop = node.scrollHeight;
  }, [messages, open]);

  async function handleSubmit(event, nextMessage = null) {
    if (event) {
      event.preventDefault();
    }

    const content = (nextMessage ?? draft).trim();
    if (!content || loading) {
      return;
    }

    const userMessage = {
      id: `user-${Date.now()}`,
      role: "user",
      content,
      keywords: [],
      suggestedLinks: [],
      followUpQuestions: [],
      recommendedCourses: [],
      recommendedProjects: [],
    };

    const loadingMessage = {
      id: `assistant-loading-${Date.now()}`,
      role: "assistant",
      content: "답변을 준비하고 있습니다...",
      loading: true,
      keywords: [],
      suggestedLinks: [],
      followUpQuestions: [],
      recommendedCourses: [],
      recommendedProjects: [],
    };

    const nextMessages = [...messages, userMessage, loadingMessage];
    setMessages(nextMessages);
    setDraft("");
    setError("");
    setLoading(true);

    try {
      const response = await sendChatbotMessage({
        message: content,
        history: toHistory([...messages, userMessage]),
        currentPath,
      });

      setMessages((current) =>
        current.map((message) =>
          message.id === loadingMessage.id
            ? {
                id: `assistant-${Date.now()}`,
                role: "assistant",
                content: response.answer,
                intentType: response.intentType,
                keywords: response.keywords || [],
                suggestedLinks: response.suggestedLinks || [],
                followUpQuestions: response.followUpQuestions || [],
                recommendedCourses: response.recommendedCourses || [],
                recommendedProjects: response.recommendedProjects || [],
              }
            : message,
        ),
      );
    } catch (sendError) {
      setMessages((current) => current.filter((message) => message.id !== loadingMessage.id));
      setError(sendError.message);
    } finally {
      setLoading(false);
    }
  }

  function handleOpenLink(link) {
    navigate(link.url);
    onClose();
  }

  function handleOpenProject(projectPostId) {
    navigate(`/projects/${projectPostId}`);
    onClose();
  }

  if (!open) {
    return null;
  }

  return (
    <div className="chatbot-backdrop" role="presentation" onClick={onClose}>
      <aside
        className="chatbot-drawer"
        role="dialog"
        aria-modal="true"
        aria-labelledby="chatbot-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="chatbot-header">
          <div className="list-stack compact-list">
            <p className="eyebrow">AI 도우미</p>
            <h2 id="chatbot-title">KIACMS Copilot</h2>
            <span className="muted-text">
              {getRoleLabel(user?.roleType)} 기준으로 사이트 사용법과 추천을 안내합니다.
            </span>
          </div>
          <button className="ghost-button button-small" type="button" onClick={onClose}>
            닫기
          </button>
        </div>

        <div className="chatbot-quick-panel">
          <strong>빠른 질문</strong>
          <div className="chatbot-link-grid">
            {quickPrompts.map((prompt) => (
              <button
                key={prompt}
                type="button"
                className="ghost-button button-small chatbot-action-button"
                onClick={() => handleSubmit(null, prompt)}
              >
                {prompt}
              </button>
            ))}
          </div>
        </div>

        <div ref={scrollContainerRef} className="chatbot-message-list">
          {messages.map((message) => (
            <MessageBubble
              key={message.id}
              message={message}
              onFollowUp={(question) => handleSubmit(null, question)}
              onOpenLink={handleOpenLink}
              onOpenProject={handleOpenProject}
            />
          ))}
        </div>

        {error ? <div className="form-alert error">{error}</div> : null}

        <form className="chatbot-form" onSubmit={handleSubmit}>
          <FormField
            label="질문 입력"
            hint="사이트 사용법, 진로 기반 강좌 추천, 프로젝트 계획서 분석까지 자연스럽게 물어보세요."
          >
            <textarea
              rows={3}
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              disabled={loading}
            />
          </FormField>

          <div className="button-row align-end">
            <button className="primary-button button-small" type="submit" disabled={loading || !draft.trim()}>
              {loading ? "답변 생성 중..." : "전송"}
            </button>
          </div>
        </form>
      </aside>
    </div>
  );
}
