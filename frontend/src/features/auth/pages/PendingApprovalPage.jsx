import { Link, useLocation } from "react-router-dom";
import StatusBadge from "../../../components/common/StatusBadge";
import { useAuth } from "../../../hooks/useAuth";
import { getRoleLabel } from "../../../utils/userLabels";

function getStatusCopy(status, message) {
  switch (status) {
    case "REJECTED":
      return {
        title: "가입 신청이 거절되었습니다.",
        description:
          message ||
          "입력한 정보를 다시 확인한 뒤 새로 가입 신청하거나 관리자에게 문의해 주세요.",
      };
    case "WITHDRAWN":
      return {
        title: "탈퇴 처리된 계정입니다.",
        description:
          message ||
          "탈퇴 처리된 계정은 다시 사용할 수 없습니다. 필요한 경우 새 계정 가입 또는 관리자 문의가 필요합니다.",
      };
    case "PENDING":
    default:
      return {
        title: "가입 신청이 접수되었습니다.",
        description:
          message ||
          "Root 관리자 승인 전까지는 로그인과 핵심 기능 접근이 제한됩니다. 승인 결과는 로그인 후 알림에서 확인할 수 있습니다.",
      };
  }
}

export default function PendingApprovalPage() {
  const location = useLocation();
  const { pendingSignup, logout } = useAuth();
  const info = location.state || pendingSignup || {};
  const status = info.status || "PENDING";
  const copy = getStatusCopy(status, info.message);

  return (
    <div className="auth-page">
      <div className="panel pending-panel">
        <div className="list-stack pending-stack">
          <div className="list-stack compact-list">
            <p className="eyebrow">계정 상태 안내</p>
            <h1>{copy.title}</h1>
            <p className="pending-description">{copy.description}</p>
          </div>

          <div className="summary-card pending-summary-card">
            <div>
              <span>이메일</span>
              <strong>{info.email || "-"}</strong>
            </div>
            <div>
              <span>이름</span>
              <strong>{info.name || "-"}</strong>
            </div>
            <div>
              <span>역할</span>
              <strong>{info.roleType ? getRoleLabel(info.roleType) : "-"}</strong>
            </div>
            <div>
              <span>상태</span>
              <strong>
                <StatusBadge value={status} />
              </strong>
            </div>
          </div>

          {info.message ? <div className="form-alert">{info.message}</div> : null}

          <div className="inline-actions pending-actions">
            <Link className="primary-button" to="/login">
              로그인 화면으로
            </Link>
            <Link className="ghost-button" to="/signup">
              새로 가입하기
            </Link>
            <button className="ghost-button" type="button" onClick={logout}>
              로그아웃
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
