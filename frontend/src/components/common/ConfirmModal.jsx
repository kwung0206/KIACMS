import { useEffect } from "react";

export default function ConfirmModal({
  open,
  title,
  description,
  confirmLabel,
  cancelLabel = "취소",
  tone = "primary",
  onConfirm,
  onCancel,
}) {
  useEffect(() => {
    if (!open) {
      return undefined;
    }

    function handleKeyDown(event) {
      if (event.key === "Escape") {
        onCancel();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open, onCancel]);

  if (!open) {
    return null;
  }

  return (
    <div className="modal-backdrop" role="presentation" onClick={onCancel}>
      <div
        className="modal-dialog modal-dialog-small"
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div>
            <h2 id="confirm-modal-title">{title}</h2>
            <p className="muted-text">{description}</p>
          </div>
        </div>

        <div className="button-row align-end">
          <button className="ghost-button button-small" type="button" onClick={onCancel}>
            {cancelLabel}
          </button>
          <button
            className={`${tone === "danger" ? "danger-button" : "primary-button"} button-small`}
            type="button"
            onClick={onConfirm}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
