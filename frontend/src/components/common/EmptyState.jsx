export default function EmptyState({ title, description, action }) {
  return (
    <div className="panel empty-state">
      <h3>{title}</h3>
      <p>{description}</p>
      {action ? <div className="empty-state-action">{action}</div> : null}
    </div>
  );
}
