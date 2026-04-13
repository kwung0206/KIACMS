export default function LoadingScreen({ message = "불러오는 중입니다." }) {
  return (
    <div className="centered-state">
      <div className="loading-spinner" />
      <p>{message}</p>
    </div>
  );
}
