import { getStatusLabel } from "../../utils/userLabels";

const toneMap = {
  APPROVED: "positive",
  ACCEPTED: "positive",
  COMPLETED: "positive",
  PENDING: "warning",
  SUBMITTED: "warning",
  IN_PROGRESS: "warning",
  REJECTED: "danger",
  WITHDRAWN: "muted",
  CLOSED: "muted",
  ARCHIVED: "muted",
  DRAFT: "muted",
  OPEN: "info",
  ACTIVE: "info",
  SCHEDULED: "info",
  READ: "muted",
  UNREAD: "warning",
};

export default function StatusBadge({ value }) {
  if (!value) {
    return <span className="status-badge muted">-</span>;
  }

  const tone = toneMap[value] || "neutral";
  return <span className={`status-badge ${tone}`}>{getStatusLabel(value)}</span>;
}
