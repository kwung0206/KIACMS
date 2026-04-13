import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchTaggedNotes } from "../../../api/noteApi";
import EmptyState from "../../../components/common/EmptyState";
import LoadingScreen from "../../../components/common/LoadingScreen";
import PageHeader from "../../../components/common/PageHeader";
import { formatDateTime } from "../../../utils/date";

export default function InstructorTaggedNotesPage() {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const response = await fetchTaggedNotes();
        if (active) {
          setNotes(response);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return <LoadingScreen message="태그된 정리글 목록을 불러오는 중..." />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="태그된 정리글 목록"
        description="학생이 나를 태그한 정리글을 확인하고 상세 화면으로 이동해 코멘트를 남길 수 있습니다."
      />

      {notes.length === 0 ? (
        <EmptyState
          title="태그된 정리글이 없습니다."
          description="학생이 강사를 태그하면 여기에 목록이 표시됩니다."
        />
      ) : (
        <div className="panel table-panel">
          <table className="data-table">
            <thead>
              <tr>
                <th>정리글</th>
                <th>작성자</th>
                <th>과정</th>
                <th>태그 시각</th>
                <th>코멘트 수</th>
              </tr>
            </thead>
            <tbody>
              {notes.map((note) => (
                <tr key={note.noteId}>
                  <td>
                    <Link to={`/instructor/tagged-notes/${note.noteId}`}>{note.noteTitle}</Link>
                  </td>
                  <td>{note.authorName}</td>
                  <td>{note.courseTitle}</td>
                  <td>{formatDateTime(note.taggedAt)}</td>
                  <td>{note.commentCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
