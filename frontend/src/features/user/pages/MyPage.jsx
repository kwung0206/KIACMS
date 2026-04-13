import { useEffect, useState } from "react";
import {
  changeMyPassword,
  fetchMyProfile,
  updateMyProfile,
} from "../../../api/userApi";
import FormField from "../../../components/common/FormField";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { useAuth } from "../../../hooks/useAuth";
import { useTheme } from "../../../hooks/useTheme";
import { formatDateTime } from "../../../utils/date";
import { getRoleLabel } from "../../../utils/userLabels";

const initialPasswordForm = {
  currentPassword: "",
  newPassword: "",
  newPasswordConfirm: "",
};

export default function MyPage() {
  const { refreshProfile } = useAuth();
  const { theme, setTheme } = useTheme();
  const [profile, setProfile] = useState(null);
  const [profileForm, setProfileForm] = useState({
    name: "",
    phoneNumber: "",
    bio: "",
  });
  const [passwordForm, setPasswordForm] = useState(initialPasswordForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [profileMessage, setProfileMessage] = useState({ type: "", text: "" });
  const [passwordMessage, setPasswordMessage] = useState({ type: "", text: "" });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    let active = true;

    async function loadProfile() {
      try {
        const response = await fetchMyProfile();
        if (!active) {
          return;
        }

        setProfile(response);
        setProfileForm({
          name: response.name || "",
          phoneNumber: response.phoneNumber || "",
          bio: response.bio || "",
        });
      } catch (loadError) {
        if (active) {
          setError(loadError.message);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadProfile();
    return () => {
      active = false;
    };
  }, []);

  function handleProfileChange(event) {
    const { name, value } = event.target;
    setProfileForm((current) => ({ ...current, [name]: value }));
  }

  function handlePasswordChange(event) {
    const { name, value } = event.target;
    setPasswordForm((current) => ({ ...current, [name]: value }));
  }

  async function handleProfileSubmit(event) {
    event.preventDefault();
    setSavingProfile(true);
    setProfileMessage({ type: "", text: "" });

    try {
      const updatedProfile = await updateMyProfile(profileForm);
      setProfile(updatedProfile);
      await refreshProfile();
      setProfileMessage({ type: "success", text: "회원정보가 저장되었습니다." });
    } catch (saveError) {
      setProfileMessage({ type: "error", text: saveError.message });
    } finally {
      setSavingProfile(false);
    }
  }

  async function handlePasswordSubmit(event) {
    event.preventDefault();
    setSavingPassword(true);
    setPasswordMessage({ type: "", text: "" });

    if (passwordForm.newPassword !== passwordForm.newPasswordConfirm) {
      setPasswordMessage({ type: "error", text: "새 비밀번호와 비밀번호 확인이 일치하지 않습니다." });
      setSavingPassword(false);
      return;
    }

    try {
      await changeMyPassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordForm(initialPasswordForm);
      setPasswordMessage({ type: "success", text: "비밀번호가 변경되었습니다." });
    } catch (saveError) {
      setPasswordMessage({ type: "error", text: saveError.message });
    } finally {
      setSavingPassword(false);
    }
  }

  if (loading) {
    return <LoadingScreen message="마이페이지를 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="마이페이지"
        description="기본 정보, 자기소개, 비밀번호, 테마 설정을 한 곳에서 관리할 수 있습니다."
      />

      {error ? <div className="form-alert error">{error}</div> : null}

      <section className="detail-grid">
        <article className="panel">
          <h2>기본 정보</h2>
          <dl className="detail-list">
            <div>
              <dt>이메일</dt>
              <dd>{profile?.email || "-"}</dd>
            </div>
            <div>
              <dt>역할</dt>
              <dd>{getRoleLabel(profile?.roleType)}</dd>
            </div>
            <div>
              <dt>상태</dt>
              <dd>{profile ? <StatusBadge value={profile.status} /> : "-"}</dd>
            </div>
            <div>
              <dt>마지막 로그인</dt>
              <dd>{formatDateTime(profile?.lastLoginAt)}</dd>
            </div>
          </dl>
        </article>

        <article className="panel">
          <h2>테마 설정</h2>
          <div className="settings-card">
            <p className="muted-text">다크모드와 라이트모드는 마이페이지에서만 변경할 수 있습니다.</p>
            <div className="segmented-control">
              <button
                type="button"
                className={theme === "light" ? "segmented-button active" : "segmented-button"}
                onClick={() => setTheme("light")}
              >
                라이트 모드
              </button>
              <button
                type="button"
                className={theme === "dark" ? "segmented-button active" : "segmented-button"}
                onClick={() => setTheme("dark")}
              >
                다크 모드
              </button>
            </div>
          </div>
        </article>
      </section>

      <section className="detail-grid">
        <article className="panel">
          <h2>회원정보 수정</h2>
          <form className="form-stack" onSubmit={handleProfileSubmit}>
            <FormField label="이메일" hint="이메일은 로그인 식별자라서 수정할 수 없습니다.">
              <input value={profile?.email || ""} readOnly disabled />
            </FormField>

            <FormField label="이름">
              <input name="name" value={profileForm.name} onChange={handleProfileChange} required />
            </FormField>

            <FormField label="전화번호">
              <input
                name="phoneNumber"
                value={profileForm.phoneNumber}
                onChange={handleProfileChange}
              />
            </FormField>

            <FormField label="자기소개" hint="프로젝트 지원이나 프로필 노출에 활용할 수 있는 소개를 작성해 주세요.">
              <textarea name="bio" value={profileForm.bio} onChange={handleProfileChange} rows={5} />
            </FormField>

            {profileMessage.text ? (
              <div className={profileMessage.type === "error" ? "form-alert error" : "form-alert"}>
                {profileMessage.text}
              </div>
            ) : null}

            <div className="button-row">
              <button className="primary-button button-small" type="submit" disabled={savingProfile}>
                {savingProfile ? "저장 중..." : "회원정보 저장"}
              </button>
            </div>
          </form>
        </article>

        <article className="panel">
          <h2>비밀번호 변경</h2>
          <form className="form-stack" onSubmit={handlePasswordSubmit}>
            <FormField label="현재 비밀번호">
              <input
                type="password"
                name="currentPassword"
                value={passwordForm.currentPassword}
                onChange={handlePasswordChange}
                autoComplete="current-password"
                required
              />
            </FormField>

            <FormField label="새 비밀번호" hint="8자 이상으로 입력해 주세요.">
              <input
                type="password"
                name="newPassword"
                value={passwordForm.newPassword}
                onChange={handlePasswordChange}
                autoComplete="new-password"
                required
              />
            </FormField>

            <FormField label="새 비밀번호 확인">
              <input
                type="password"
                name="newPasswordConfirm"
                value={passwordForm.newPasswordConfirm}
                onChange={handlePasswordChange}
                autoComplete="new-password"
                required
              />
            </FormField>

            {passwordMessage.text ? (
              <div className={passwordMessage.type === "error" ? "form-alert error" : "form-alert"}>
                {passwordMessage.text}
              </div>
            ) : null}

            <div className="button-row">
              <button className="primary-button button-small" type="submit" disabled={savingPassword}>
                {savingPassword ? "변경 중..." : "비밀번호 변경"}
              </button>
            </div>
          </form>
        </article>
      </section>
    </div>
  );
}
