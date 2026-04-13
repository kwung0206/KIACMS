const fs = require("fs");
const path = require("path");
const { chromium } = require("playwright");

const baseUrl = "http://localhost:5173";
const outputDir = "C:/Users/kgj01/Documents/KIACMS/artifacts/ui-check/stage1";
fs.mkdirSync(outputDir, { recursive: true });

function uniqueStamp() {
  const now = new Date();
  const pad = (value) => String(value).padStart(2, "0");
  return [
    now.getFullYear(),
    pad(now.getMonth() + 1),
    pad(now.getDate()),
    pad(now.getHours()),
    pad(now.getMinutes()),
    pad(now.getSeconds()),
  ].join("");
}

function todayInfo() {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Seoul",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());
  const year = parts.find((part) => part.type === "year")?.value;
  const month = parts.find((part) => part.type === "month")?.value;
  const day = parts.find((part) => part.type === "day")?.value;
  return {
    key: `${year}-${month}-${day}`,
    dayNumber: String(Number(day)),
  };
}

async function createContext(browser) {
  return browser.newContext({
    viewport: { width: 1440, height: 1200 },
    locale: "ko-KR",
    timezoneId: "Asia/Seoul",
  });
}

async function login(page, email, password, urlPattern) {
  await page.goto(`${baseUrl}/login`, { waitUntil: "networkidle" });
  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="password"]').fill(password);
  await page.getByRole("button", { name: "로그인" }).click();
  await page.waitForURL(urlPattern, { timeout: 20000 });
  await page.waitForLoadState("networkidle");
}

async function fillProjectCreateForm(page, title) {
  await page.locator(".sidebar-nav").getByRole("link", { name: "프로젝트" }).click();
  await page.locator(".page-header h1").filter({ hasText: "프로젝트 게시판" }).first().waitFor({ timeout: 15000 });
  await page.getByRole("link", { name: "새 모집글 작성" }).click();
  await page.locator(".page-header h1").filter({ hasText: "새 모집글 작성" }).first().waitFor({ timeout: 15000 });

  await page.getByLabel("프로젝트 제목").fill(title);
  await page.getByLabel("프로젝트 소개").fill("브라우저 렌더링 검증을 위한 프로젝트 소개입니다.");
  await page.getByLabel("목표").fill("학생 지원서 CRUD와 리스트형 게시판 화면을 검증합니다.");
  await page.getByLabel("기술 스택").fill("React, Spring Boot, PostgreSQL");
  await page.getByLabel("예상 진행 기간").fill("4주");
  await page.getByLabel("연락처").fill("verify-student@kiacms.local");
  await page.getByLabel("PM 소개").fill("검증용 계정이 작성한 샘플 모집글입니다.");
  await page.getByLabel("PM 경험 및 강점").fill("기획과 일정 조율 경험이 있으며 빠른 피드백이 가능합니다.");
  await page.getByLabel("포지션명").first().fill("프론트엔드");
  await page.getByLabel("포지션 설명").first().fill("React 기반 화면 구현과 UI 개선을 담당합니다.");
  await page.getByLabel("필수 역량").first().fill("React, Vite, 상태관리");

  await page.screenshot({
    path: path.join(outputDir, "student-project-create.png"),
    fullPage: true,
  });

  await page.getByRole("button", { name: "모집글 등록" }).click();
  await page.waitForURL(/http:\/\/localhost:5173\/projects\/[^/]+$/, { timeout: 20000 });
  await page.waitForLoadState("networkidle");
}

(async () => {
  const browser = await chromium.launch({
    channel: "msedge",
    headless: true,
  });

  const report = {
    accounts: {},
    screenshots: {},
  };

  const stamp = uniqueStamp();
  const newStudentEmail = `stage1-${stamp}@kiacms.local`;
  const newStudentName = `검증학생${stamp.slice(-4)}`;
  const projectTitle = `1차 검증 프로젝트 ${stamp}`;
  const koreaToday = todayInfo();

  report.generated = {
    newStudentEmail,
    newStudentName,
    projectTitle,
    koreaToday,
  };

  try {
    const signupContext = await createContext(browser);
    const signupPage = await signupContext.newPage();
    await signupPage.goto(`${baseUrl}/signup`, { waitUntil: "networkidle" });
    await signupPage.getByLabel("이름").fill(newStudentName);
    await signupPage.getByLabel("이메일").fill(newStudentEmail);
    await signupPage.locator('input[name="password"]').fill("Test1234!");
    await signupPage.locator('input[name="passwordConfirm"]').fill("Test1234!");
    await signupPage.getByLabel("전화번호").fill(`010${stamp.slice(-8)}`);
    await signupPage.getByLabel("역할").selectOption("STUDENT");
    await signupPage.getByRole("button", { name: "가입 신청" }).click();
    await signupPage.waitForURL(/\/pending$/, { timeout: 20000 });
    await signupPage.waitForLoadState("networkidle");
    report.pending = {
      title: (await signupPage.getByRole("heading", { name: "가입 신청이 접수되었습니다." }).innerText()).trim(),
      description: (await signupPage.locator(".pending-description").innerText()).trim(),
    };
    await signupPage.screenshot({
      path: path.join(outputDir, "pending-signup.png"),
      fullPage: true,
    });
    report.screenshots.pending = path.join(outputDir, "pending-signup.png");
    await signupContext.close();

    const rootContext = await createContext(browser);
    const rootPage = await rootContext.newPage();
    await login(rootPage, "root@kiacms.local", "Test1234!", /\/root$/);
    const pendingCard = rootPage.locator("article").filter({ hasText: newStudentEmail }).first();
    await pendingCard.waitFor({ timeout: 20000 });
    report.accounts.root = {
      topbar: (await rootPage.locator(".topbar-user").innerText()).trim(),
    };
    await rootPage.screenshot({
      path: path.join(outputDir, "root-home-pending.png"),
      fullPage: true,
    });
    report.screenshots.rootPending = path.join(outputDir, "root-home-pending.png");
    await pendingCard.getByRole("button", { name: "승인" }).click();
    await rootPage.getByText("가입 신청이 승인되었습니다.").waitFor({ timeout: 10000 });

    await rootPage.locator(".sidebar-nav").getByRole("link", { name: "수업 일정 관리" }).click();
    await rootPage.locator(".page-header h1").filter({ hasText: "수업 일정 관리" }).first().waitFor({ timeout: 15000 });
    report.rootCourses = {
      topbar: (await rootPage.locator(".topbar-user").innerText()).trim(),
      pageTitle: (await rootPage.locator(".page-header h1").innerText()).trim(),
    };
    await rootPage.screenshot({
      path: path.join(outputDir, "root-courses.png"),
      fullPage: true,
    });
    report.screenshots.rootCourses = path.join(outputDir, "root-courses.png");
    await rootContext.close();

    const createdStudentContext = await createContext(browser);
    const createdStudentPage = await createdStudentContext.newPage();
    await login(createdStudentPage, newStudentEmail, "Test1234!", /\/student$/);
    report.accounts.createdStudent = {
      topbar: (await createdStudentPage.locator(".topbar-user").innerText()).trim(),
    };
    await fillProjectCreateForm(createdStudentPage, projectTitle);
    report.createdProjectUrl = createdStudentPage.url();
    await createdStudentContext.close();

    const studentContext = await createContext(browser);
    const studentPage = await studentContext.newPage();
    await login(studentPage, "student@kiacms.local", "Test1234!", /\/student$/);
    report.accounts.student = {
      topbar: (await studentPage.locator(".topbar-user").innerText()).trim(),
    };

    await studentPage.locator(".sidebar-nav").getByRole("link", { name: "수업 캘린더" }).click();
    await studentPage.locator(".page-header h1").filter({ hasText: "학생 수업 캘린더" }).first().waitFor({ timeout: 15000 });
    const selectedDay = (await studentPage.locator(".calendar-day-selected .calendar-day-number").innerText()).trim();
    report.studentCalendar = {
      selectedDay,
      expectedDay: koreaToday.dayNumber,
      matchesKoreaToday: selectedDay === koreaToday.dayNumber,
    };
    await studentPage.screenshot({
      path: path.join(outputDir, "student-calendar.png"),
      fullPage: true,
    });
    report.screenshots.studentCalendar = path.join(outputDir, "student-calendar.png");

    await studentPage.locator(".sidebar-nav").getByRole("link", { name: "프로젝트" }).click();
    await studentPage.locator(".page-header h1").filter({ hasText: "프로젝트 게시판" }).first().waitFor({ timeout: 15000 });
    const boardRow = studentPage.locator(".project-list-row").filter({ hasText: projectTitle }).first();
    await boardRow.waitFor({ timeout: 15000 });
    await boardRow.click();
    await studentPage.locator(".project-detail-preview").getByRole("heading", { name: projectTitle }).waitFor({ timeout: 15000 });
    report.projectBoard = {
      selectedTitle: (await studentPage.locator(".project-detail-preview h2").innerText()).trim(),
    };
    await studentPage.screenshot({
      path: path.join(outputDir, "projects-board-list-detail.png"),
      fullPage: true,
    });
    report.screenshots.projectBoard = path.join(outputDir, "projects-board-list-detail.png");

    await studentPage.getByRole("button", { name: "지원하러 가기" }).click();
    await studentPage.waitForURL(/\/projects\/.+$/, { timeout: 15000 });
    await studentPage.locator("form").getByRole("heading", { name: "학생 지원서 작성" }).waitFor({ timeout: 15000 });
    await studentPage.getByLabel("지원 동기").fill("기존에 제출한 내용을 다시 보고 수정/삭제까지 검증하기 위해 지원합니다.");
    await studentPage.getByLabel("수강 이력").fill("웹 풀스택 과정 5개월");
    await studentPage.getByLabel("자격증 및 증빙").fill("정보처리기사 필기 합격");
    await studentPage.getByLabel("기술 스택").fill("React, TypeScript, Spring Boot");
    await studentPage.getByLabel("포트폴리오 URL").fill("https://example.com/student-portfolio");
    await studentPage.getByLabel("자기소개").fill("빠르게 화면을 개선하고 사용자 흐름을 정리하는 데 강점이 있습니다.");
    await studentPage.getByRole("button", { name: "지원서 제출" }).click();
    await studentPage.getByText("지원서가 제출되었습니다.").waitFor({ timeout: 15000 });

    await studentPage.getByRole("link", { name: "내 지원 현황" }).click();
    await studentPage.locator(".page-header h1").filter({ hasText: "내 지원 현황" }).first().waitFor({ timeout: 15000 });
    const applicationRow = studentPage.locator(".project-list-row").filter({ hasText: projectTitle }).first();
    await applicationRow.waitFor({ timeout: 15000 });
    await applicationRow.click();
    await studentPage.locator(".project-detail-preview").getByRole("heading", { name: projectTitle }).waitFor({ timeout: 15000 });
    report.applicationView = {
      detailTitle: (await studentPage.locator(".project-detail-preview h2").innerText()).trim(),
      beforeDeleteCount: await studentPage.locator(".project-list-row").count(),
    };
    await studentPage.screenshot({
      path: path.join(outputDir, "student-applications-detail.png"),
      fullPage: true,
    });
    report.screenshots.studentApplications = path.join(outputDir, "student-applications-detail.png");

    await studentPage.getByRole("button", { name: "지원서 수정" }).click();
    await studentPage.getByRole("heading", { name: "지원서 수정" }).waitFor({ timeout: 10000 });
    await studentPage.getByLabel("지원 동기").fill("수정 기능까지 확인하기 위해 지원서를 다시 정리했습니다.");
    await studentPage.getByLabel("기술 스택").fill("React, Vite, Spring Boot, PostgreSQL");
    await studentPage.getByRole("button", { name: "수정 저장" }).click();
    await studentPage.getByText("지원서가 수정되었습니다.").waitFor({ timeout: 15000 });
    report.applicationUpdate = {
      successMessage: "지원서가 수정되었습니다.",
    };

    await studentPage.getByRole("button", { name: "지원서 삭제" }).click();
    await studentPage.getByRole("dialog").waitFor({ timeout: 5000 });
    await studentPage.getByRole("dialog").getByRole("button", { name: "삭제", exact: true }).click();
    await studentPage.getByText("지원서가 삭제되었습니다.").waitFor({ timeout: 15000 });
    report.applicationDelete = {
      afterDeleteCount: await studentPage.locator(".project-list-row").count(),
    };

    await studentPage.locator(".sidebar-nav").getByRole("link", { name: "알림" }).click();
    await studentPage.locator(".page-header h1").filter({ hasText: "알림" }).first().waitFor({ timeout: 15000 });
    const notificationCardsBefore = await studentPage.locator(".info-card").count();
    const deleteButton = studentPage.getByRole("button", { name: "삭제" }).first();
    if (notificationCardsBefore > 0) {
      await deleteButton.click();
      await studentPage.getByText("알림이 삭제되었습니다.").waitFor({ timeout: 15000 });
    }
    const notificationCardsAfter = await studentPage.locator(".info-card").count();
    report.notifications = {
      before: notificationCardsBefore,
      after: notificationCardsAfter,
      deleted: notificationCardsAfter < notificationCardsBefore,
    };
    await studentPage.screenshot({
      path: path.join(outputDir, "notifications-page.png"),
      fullPage: true,
    });
    report.screenshots.notifications = path.join(outputDir, "notifications-page.png");
    await studentContext.close();

    const ownerNotificationContext = await createContext(browser);
    const ownerNotificationPage = await ownerNotificationContext.newPage();
    await login(ownerNotificationPage, newStudentEmail, "Test1234!", /\/student$/);
    await ownerNotificationPage.locator(".sidebar-nav").getByRole("link", { name: "알림" }).click();
    await ownerNotificationPage.locator(".page-header h1").filter({ hasText: "알림" }).first().waitFor({ timeout: 15000 });
    const ownerNotificationCountBefore = await ownerNotificationPage.locator(".info-card").count();
    if (ownerNotificationCountBefore > 0) {
      const firstNotificationCard = ownerNotificationPage.locator("article.info-card").first();
      await firstNotificationCard.getByRole("button", { name: "삭제", exact: true }).click();
      await ownerNotificationPage.waitForFunction(
        (before) => document.querySelectorAll("article.info-card").length < before,
        ownerNotificationCountBefore,
        { timeout: 15000 },
      );
    }
    const ownerNotificationCountAfter = await ownerNotificationPage.locator(".info-card").count();
    report.notifications = {
      before: ownerNotificationCountBefore,
      after: ownerNotificationCountAfter,
      deleted: ownerNotificationCountAfter < ownerNotificationCountBefore,
    };
    await ownerNotificationPage.screenshot({
      path: path.join(outputDir, "notifications-page-owner.png"),
      fullPage: true,
    });
    report.screenshots.notifications = path.join(outputDir, "notifications-page-owner.png");
    await ownerNotificationContext.close();

    const teacherContext = await createContext(browser);
    const teacherPage = await teacherContext.newPage();
    await login(teacherPage, "teacher@kiacms.local", "Test1234!", /\/instructor$/);
    report.accounts.teacher = {
      topbar: (await teacherPage.locator(".topbar-user").innerText()).trim(),
    };
    await teacherPage.screenshot({
      path: path.join(outputDir, "teacher-home.png"),
      fullPage: true,
    });
    report.screenshots.teacher = path.join(outputDir, "teacher-home.png");
    await teacherContext.close();

    const mentorContext = await createContext(browser);
    const mentorPage = await mentorContext.newPage();
    await login(mentorPage, "mentor@kiacms.local", "Test1234!", /\/mentor$/);
    report.accounts.mentor = {
      topbar: (await mentorPage.locator(".topbar-user").innerText()).trim(),
    };
    await mentorPage.screenshot({
      path: path.join(outputDir, "mentor-home.png"),
      fullPage: true,
    });
    report.screenshots.mentor = path.join(outputDir, "mentor-home.png");
    await mentorContext.close();
  } catch (error) {
    report.error = {
      message: error.message,
      stack: error.stack,
    };
  } finally {
    await browser.close();
    fs.writeFileSync(path.join(outputDir, "report.json"), JSON.stringify(report, null, 2));
    console.log(JSON.stringify(report, null, 2));
  }
})();
