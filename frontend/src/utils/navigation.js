export function getHomePathByRole(role) {
  switch (role) {
    case "STUDENT":
      return "/student";
    case "INSTRUCTOR":
      return "/instructor";
    case "MENTOR":
      return "/mentor";
    case "ROOT":
      return "/root";
    default:
      return "/login";
  }
}

export function matchesPath(pathname, matchers = []) {
  return matchers.some((matcher) => {
    if (matcher.endsWith("/*")) {
      return pathname.startsWith(matcher.slice(0, -1));
    }

    return pathname === matcher;
  });
}

export function isNavigationItemActive(item, pathname) {
  return matchesPath(pathname, item.matchers || [item.to]);
}

export function getNavigationItems(role) {
  switch (role) {
    case "STUDENT":
      return [
        { label: "홈", to: "/student", matchers: ["/student"] },
        {
          label: "수업 캘린더",
          to: "/student/calendar",
          matchers: ["/student/calendar", "/student/sessions/*"],
        },
        {
          label: "내 노트",
          to: "/student/notes",
          matchers: ["/student/notes", "/student/notes/*"],
        },
        {
          label: "프로젝트",
          to: "/projects",
          matchers: ["/projects", "/projects/*", "/student/projects/*", "/student/applications"],
        },
        { label: "알림", to: "/notifications", matchers: ["/notifications"] },
        { label: "마이페이지", to: "/me", matchers: ["/me"] },
      ];
    case "INSTRUCTOR":
      return [
        { label: "홈", to: "/instructor", matchers: ["/instructor"] },
        {
          label: "회차 관리",
          to: "/instructor/sessions",
          matchers: ["/instructor/sessions", "/instructor/sessions/*"],
        },
        {
          label: "태그된 노트",
          to: "/instructor/tagged-notes",
          matchers: ["/instructor/tagged-notes", "/instructor/tagged-notes/*"],
        },
        {
          label: "프로젝트 지원",
          to: "/projects",
          matchers: ["/projects", "/projects/*", "/instructor/project-mentoring"],
        },
        { label: "알림", to: "/notifications", matchers: ["/notifications"] },
        { label: "마이페이지", to: "/me", matchers: ["/me"] },
      ];
    case "MENTOR":
      return [
        { label: "홈", to: "/mentor", matchers: ["/mentor"] },
        {
          label: "담당 학생 관리",
          to: "/mentor/students",
          matchers: ["/mentor/students", "/mentor/students/*"],
        },
        { label: "마이페이지", to: "/me", matchers: ["/me"] },
      ];
    case "ROOT":
      return [
        { label: "홈", to: "/root", matchers: ["/root"] },
        {
          label: "수업 일정 관리",
          to: "/root/courses",
          matchers: ["/root/courses", "/root/courses/*"],
        },
        {
          label: "프로젝트 삭제 관리",
          to: "/root/projects",
          matchers: ["/root/projects", "/root/projects/*"],
        },
        { label: "알림", to: "/notifications", matchers: ["/notifications"] },
        { label: "마이페이지", to: "/me", matchers: ["/me"] },
      ];
    default:
      return [{ label: "마이페이지", to: "/me", matchers: ["/me"] }];
  }
}
