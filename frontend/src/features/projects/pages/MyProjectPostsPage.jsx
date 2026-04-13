import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchMyProjectPosts } from "../../../api/projectApi";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import StatusBadge from "../../../components/common/StatusBadge";
import { formatDate } from "../../../utils/date";

function truncateText(value, maxLength = 120) {
  if (!value || value.length <= maxLength) {
    return value || "";
  }

  return `${value.slice(0, maxLength).trim()}...`;
}

export default function MyProjectPostsPage() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function loadPosts() {
      try {
        const response = await fetchMyProjectPosts();
        if (active) {
          setPosts(response);
        }
      } catch (loadError) {
        if (active) {
          setPosts([]);
          setError(loadError.message);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadPosts();
    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return <LoadingScreen message="내 모집글을 불러오는 중입니다." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="내 모집글"
        description="내가 등록한 프로젝트 모집글을 확인하고 지원서를 관리할 수 있습니다."
        actions={
          <div className="page-action-group">
            <Link className="ghost-button button-small uniform-action-button" to="/projects">
              게시판 보기
            </Link>
            <Link className="primary-button button-small uniform-action-button" to="/student/projects/new">
              새 모집글 작성
            </Link>
          </div>
        }
      />

      {error ? <div className="form-alert error">{error}</div> : null}

      {posts.length === 0 ? (
        <EmptyState
          title="아직 등록한 모집글이 없습니다."
          description="첫 프로젝트 모집글을 작성하고 팀원을 모아 보세요."
          action={
            <Link className="primary-button button-small uniform-action-button" to="/student/projects/new">
              새 모집글 작성
            </Link>
          }
        />
      ) : (
        <div className="card-grid">
          {posts.map((post) => (
            <article key={post.id} className="panel info-card">
              <div className="spread-row">
                <strong>{post.title}</strong>
                <StatusBadge value={post.status} />
              </div>
              <p>{truncateText(post.description)}</p>
              <div className="project-meta tight">
                <small>기술 스택: {post.techStack}</small>
                <small>모집 포지션: {post.positionCount}개</small>
                <small>모집 마감: {formatDate(post.recruitUntil)}</small>
              </div>
              <div className="inline-actions">
                <Link className="ghost-button button-small uniform-action-button" to={`/projects/${post.id}`}>
                  상세 보기
                </Link>
                <Link
                  className="primary-button button-small uniform-action-button"
                  to={`/student/projects/${post.id}/manage`}
                >
                  지원서 관리
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
