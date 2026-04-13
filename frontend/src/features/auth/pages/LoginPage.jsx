import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import FormField from "../../../components/common/FormField";
import { useAuth } from "../../../hooks/useAuth";
import { getHomePathByRole } from "../../../utils/navigation";

function mapErrorCodeToStatus(code) {
  switch (code) {
    case "ACCOUNT_PENDING_APPROVAL":
      return "PENDING";
    case "ACCOUNT_REJECTED":
      return "REJECTED";
    case "ACCOUNT_WITHDRAWN":
      return "WITHDRAWN";
    default:
      return null;
  }
}

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [form, setForm] = useState({
    email: "",
    password: "",
  });
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response = await login(form);
      const user = response.user;

      if (user.status !== "APPROVED") {
        navigate("/pending", {
          replace: true,
          state: {
            email: user.email,
            name: user.name,
            roleType: user.roleType,
            status: user.status,
            message: "계정 상태를 먼저 확인해 주세요.",
          },
        });
        return;
      }

      const requestedPath = location.state?.from?.pathname;
      const destination = requestedPath || getHomePathByRole(user.roleType);
      navigate(destination, { replace: true });
    } catch (error) {
      const blockedStatus = mapErrorCodeToStatus(error.code);

      if (blockedStatus) {
        navigate("/pending", {
          replace: true,
          state: {
            email: form.email,
            status: blockedStatus,
            message: error.message,
          },
        });
        return;
      }

      setErrorMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-panel">
        <div className="auth-copy">
          <p className="eyebrow">KIACMS</p>
          <h1>교육 운영, 학습 정리, 프로젝트 협업을 한 곳에서 관리합니다.</h1>
          <p>
            학생, 강사, 멘토, Root 관리자가 같은 서비스 안에서 승인, 일정, 정리글, 운영 흐름을
            자연스럽게 이어갈 수 있도록 구성했습니다.
          </p>
        </div>

        <form className="panel auth-form form-stack" onSubmit={handleSubmit}>
          <div>
            <h2>로그인</h2>
            <p className="muted-text">승인 완료된 계정만 역할별 화면으로 이동할 수 있습니다.</p>
          </div>

          <FormField label="이메일">
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />
          </FormField>

          <FormField label="비밀번호">
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              autoComplete="current-password"
              required
            />
          </FormField>

          {errorMessage ? <div className="form-alert error">{errorMessage}</div> : null}

          <div className="button-row">
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "로그인 중..." : "로그인"}
            </button>
          </div>

          <p className="auth-footer">
            아직 계정이 없다면 <Link to="/signup">회원가입</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
