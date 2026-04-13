export function getRoleLabel(roleType) {
  switch (roleType) {
    case "ROOT":
      return "Root";
    case "STUDENT":
      return "학생";
    case "MENTOR":
      return "멘토";
    case "INSTRUCTOR":
      return "강사";
    default:
      return "사용자";
  }
}

export function getStatusLabel(status) {
  switch (status) {
    case "APPROVED":
      return "승인 완료";
    case "PENDING":
      return "승인 대기";
    case "REJECTED":
      return "반려";
    case "WITHDRAWN":
      return "탈퇴";
    case "OPEN":
      return "모집 중";
    case "CLOSED":
      return "마감";
    case "SUBMITTED":
      return "제출됨";
    case "ACCEPTED":
      return "수락";
    case "COMPLETED":
      return "완료";
    case "IN_PROGRESS":
      return "진행 중";
    case "PLANNED":
      return "준비 중";
    case "SCHEDULED":
      return "예정";
    case "CANCELLED":
      return "취소";
    case "ARCHIVED":
      return "보관";
    case "DRAFT":
      return "초안";
    case "DELETED":
      return "삭제됨";
    case "READ":
      return "읽음";
    case "UNREAD":
      return "안 읽음";
    case "ACTIVE":
      return "활성";
    default:
      return status || "-";
  }
}
