import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import FormField from "../../../components/common/FormField";
import { useAuth } from "../../../hooks/useAuth";

const roleOptions = [
  { value: "STUDENT", label: "학생" },
  { value: "INSTRUCTOR", label: "강사" },
  { value: "MENTOR", label: "멘토" },
];

export default function SignupPage() {
  const navigate = useNavigate();
  const { signup } = useAuth();
  const [form, setForm] = useState({
    email: "",
    password: "",
    passwordConfirm: "",
    name: "",
    phoneNumber: "",
    roleType: "STUDENT",
  });
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const passwordMismatch = useMemo(() => {
    if (!form.passwordConfirm) {
      return false;
    }
    return form.password !== form.passwordConfirm;
  }, [form.password, form.passwordConfirm]);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage("");

    if (passwordMismatch) {
      setErrorMessage("비밀번호가 일치하지 않습니다.");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await signup({
        email: form.email,
        password: form.password,
        name: form.name,
        phoneNumber: form.phoneNumber || null,
        roleType: form.roleType,
      });

      navigate("/pending", {
        replace: true,
        state: response,
      });
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-panel">
        <div className="auth-copy">
          <p className="eyebrow">회원가입</p>
          <h1>역할을 선택하고 승인 대기 상태로 가입을 신청합니다.</h1>
          <p>
            가입 직후에는 승인 대기 상태가 되며, Root 관리자가 신청 역할을 검토하고 승인해야 주요
            기능을 사용할 수 있습니다.
          </p>
        </div>

        <form className="panel auth-form form-stack" onSubmit={handleSubmit}>
          <div>
            <h2>가입 신청</h2>
            <p className="muted-text">
              프로필 이미지와 자기소개는 회원가입 단계가 아니라 마이페이지에서 입력할 수 있습니다.
            </p>
          </div>

          <div className="grid-two">
            <FormField label="이름">
              <input name="name" value={form.name} onChange={handleChange} required />
            </FormField>

            <FormField label="역할">
              <select name="roleType" value={form.roleType} onChange={handleChange}>
                {roleOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </FormField>
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

          <div className="grid-two">
            <FormField label="비밀번호" hint="8자 이상으로 입력해 주세요.">
              <input
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                autoComplete="new-password"
                required
              />
            </FormField>

            <FormField
              label="비밀번호 확인"
              error={passwordMismatch ? "비밀번호가 일치하지 않습니다." : ""}
            >
              <input
                name="passwordConfirm"
                type="password"
                value={form.passwordConfirm}
                onChange={handleChange}
                autoComplete="new-password"
                required
              />
            </FormField>
          </div>

          <FormField label="전화번호" hint="선택 입력 항목입니다.">
            <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange} />
          </FormField>

          {errorMessage ? <div className="form-alert error">{errorMessage}</div> : null}

          <div className="button-row">
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "가입 신청 중..." : "가입 신청"}
            </button>
          </div>

          <p className="auth-footer">
            이미 계정이 있다면 <Link to="/login">로그인</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
