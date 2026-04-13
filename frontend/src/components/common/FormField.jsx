export default function FormField({ label, hint, error, children }) {
  return (
    <label className="form-field">
      <span className="form-field-label">{label}</span>
      {children}
      {hint && !error ? <small className="form-field-hint">{hint}</small> : null}
      {error ? <small className="form-field-error">{error}</small> : null}
    </label>
  );
}
