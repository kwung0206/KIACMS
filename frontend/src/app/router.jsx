import { Navigate, Route, Routes } from "react-router-dom";
import PublicOnlyRoute from "../components/common/PublicOnlyRoute";
import ProtectedRoute from "../components/common/ProtectedRoute";
import RequireRole from "../components/common/RequireRole";
import AppShell from "../components/layout/AppShell";
import RoleHomeRedirect from "../components/navigation/RoleHomeRedirect";
import LoginPage from "../features/auth/pages/LoginPage";
import PendingApprovalPage from "../features/auth/pages/PendingApprovalPage";
import SignupPage from "../features/auth/pages/SignupPage";
import InstructorHomePage from "../features/dashboard/pages/InstructorHomePage";
import MentorHomePage from "../features/dashboard/pages/MentorHomePage";
import RootHomePage from "../features/dashboard/pages/RootHomePage";
import StudentHomePage from "../features/dashboard/pages/StudentHomePage";
import InstructorSessionsPage from "../features/course/pages/InstructorSessionsPage";
import RootCourseAdminPage from "../features/course/pages/RootCourseAdminPage";
import StudentCalendarPage from "../features/course/pages/StudentCalendarPage";
import StudentSessionDetailPage from "../features/course/pages/StudentSessionDetailPage";
import TaggedNoteDetailPage from "../features/notes/pages/TaggedNoteDetailPage";
import InstructorTaggedNotesPage from "../features/notes/pages/InstructorTaggedNotesPage";
import NoteEditorPage from "../features/notes/pages/NoteEditorPage";
import StudentNoteDetailPage from "../features/notes/pages/StudentNoteDetailPage";
import StudentNotesPage from "../features/notes/pages/StudentNotesPage";
import NotificationsPage from "../features/notifications/pages/NotificationsPage";
import MentorStudentsPage from "../features/mentor/pages/MentorStudentsPage";
import MentorApplicationsPage from "../features/projects/pages/MentorApplicationsPage";
import MyApplicationsPage from "../features/projects/pages/MyApplicationsPage";
import MyProjectPostsPage from "../features/projects/pages/MyProjectPostsPage";
import ProjectBoardPage from "../features/projects/pages/ProjectBoardPage";
import ProjectCreatePage from "../features/projects/pages/ProjectCreatePage";
import ProjectDetailPage from "../features/projects/pages/ProjectDetailPage";
import ProjectManagePage from "../features/projects/pages/ProjectManagePage";
import RootProjectModerationPage from "../features/root/pages/RootProjectModerationPage";
import MyPage from "../features/user/pages/MyPage";

export default function AppRouter() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/pending" element={<PendingApprovalPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<RoleHomeRedirect />} />
          <Route path="/me" element={<MyPage />} />

          <Route element={<RequireRole roles={["STUDENT", "INSTRUCTOR", "ROOT"]} />}>
            <Route path="/projects" element={<ProjectBoardPage />} />
            <Route path="/projects/:postId" element={<ProjectDetailPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
          </Route>

          <Route element={<RequireRole roles={["STUDENT"]} />}>
            <Route path="/student" element={<StudentHomePage />} />
            <Route path="/student/calendar" element={<StudentCalendarPage />} />
            <Route path="/student/sessions/:sessionId" element={<StudentSessionDetailPage />} />
            <Route path="/student/notes" element={<StudentNotesPage />} />
            <Route path="/student/notes/new" element={<NoteEditorPage mode="create" />} />
            <Route path="/student/notes/:noteId" element={<StudentNoteDetailPage />} />
            <Route path="/student/notes/:noteId/edit" element={<NoteEditorPage mode="edit" />} />
            <Route path="/student/projects/new" element={<ProjectCreatePage />} />
            <Route path="/student/projects/me" element={<MyProjectPostsPage />} />
            <Route path="/student/projects/:postId/manage" element={<ProjectManagePage />} />
            <Route path="/student/applications" element={<MyApplicationsPage />} />
          </Route>

          <Route element={<RequireRole roles={["INSTRUCTOR"]} />}>
            <Route path="/instructor" element={<InstructorHomePage />} />
            <Route path="/instructor/sessions" element={<InstructorSessionsPage />} />
            <Route path="/instructor/tagged-notes" element={<InstructorTaggedNotesPage />} />
            <Route path="/instructor/tagged-notes/:noteId" element={<TaggedNoteDetailPage />} />
            <Route path="/instructor/project-mentoring" element={<MentorApplicationsPage />} />
          </Route>

          <Route element={<RequireRole roles={["MENTOR"]} />}>
            <Route path="/mentor" element={<MentorHomePage />} />
            <Route path="/mentor/students" element={<MentorStudentsPage />} />
          </Route>

          <Route element={<RequireRole roles={["ROOT"]} />}>
            <Route path="/root" element={<RootHomePage />} />
            <Route path="/root/courses" element={<RootCourseAdminPage />} />
            <Route path="/root/projects" element={<RootProjectModerationPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Route>
    </Routes>
  );
}
