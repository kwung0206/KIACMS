const fs = require("fs");
const path = require("path");
const { chromium } = require("playwright");

const baseUrl = "http://localhost:5173";
const outputDir = "C:/Users/kgj01/Documents/KIACMS/artifacts/ui-check/seed-data";
fs.mkdirSync(outputDir, { recursive: true });

async function createContext(browser) {
  return browser.newContext({
    locale: "ko-KR",
    timezoneId: "Asia/Seoul",
    viewport: { width: 1440, height: 1200 },
  });
}

async function login(page, email, password, expectedPathPattern) {
  await page.goto(`${baseUrl}/login`, { waitUntil: "networkidle" });
  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="password"]').fill(password);
  await page.locator('button[type="submit"]').click();
  await page.waitForURL(new RegExp(expectedPathPattern), { timeout: 15000 });
  await page.waitForLoadState("networkidle");
}

(async () => {
  const browser = await chromium.launch({ channel: "msedge", headless: true });
  const report = {};

  const studentContext = await createContext(browser);
  const studentPage = await studentContext.newPage();
  await login(studentPage, "student@kiacms.local", "Test1234!", "/student");

  await studentPage.locator('a[href="/projects"]').click();
  await studentPage.waitForURL(/\/projects/, { timeout: 10000 });
  await studentPage.waitForTimeout(2000);
  const studentProjectsText = await studentPage.locator("body").innerText();
  report.studentProjectsVisible = [
    "캠퍼스 스터디 매칭 웹앱",
    "멀티모달 강의 요약 도우미",
    "보안 로그 이상 탐지 대시보드",
  ].every((title) => studentProjectsText.includes(title));
  await studentPage.screenshot({ path: path.join(outputDir, "student-projects.png"), fullPage: true });

  await studentPage.locator('a[href="/student/calendar"]').click();
  await studentPage.waitForURL(/\/student\/calendar/, { timeout: 10000 });
  await studentPage.waitForTimeout(2000);
  const studentCalendarText = await studentPage.locator("body").innerText();
  report.studentCalendarHasSeedCourses = ["Java 수업", "멀티모달 수업", "리눅스 수업"].every((title) =>
    studentCalendarText.includes(title),
  );
  await studentPage.screenshot({ path: path.join(outputDir, "student-calendar.png"), fullPage: true });
  await studentContext.close();

  const teacherContext = await createContext(browser);
  const teacherPage = await teacherContext.newPage();
  await login(teacherPage, "teacher@kiacms.local", "Test1234!", "/instructor");
  await teacherPage.goto(`${baseUrl}/instructor/sessions`, { waitUntil: "networkidle" });
  await teacherPage.waitForTimeout(1500);
  const teacherText = await teacherPage.locator("body").innerText();
  report.teacherSessionVisible = teacherText.includes("Java 수업 1회차") && teacherText.includes("Java 수업 15회차");
  await teacherPage.screenshot({ path: path.join(outputDir, "teacher-sessions.png"), fullPage: true });
  await teacherContext.close();

  const mentorContext = await createContext(browser);
  const mentorPage = await mentorContext.newPage();
  await login(mentorPage, "mentor@kiacms.local", "Test1234!", "/mentor");
  await mentorPage.goto(`${baseUrl}/mentor/students`, { waitUntil: "networkidle" });
  await mentorPage.waitForTimeout(1500);
  const mentorText = await mentorPage.locator("body").innerText();
  report.mentorManagedStudentsVisible = ["김민준", "이서준", "학생"].some((name) => mentorText.includes(name));
  await mentorPage.screenshot({ path: path.join(outputDir, "mentor-students.png"), fullPage: true });
  await mentorContext.close();

  const rootContext = await createContext(browser);
  const rootPage = await rootContext.newPage();
  await login(rootPage, "root@kiacms.local", "Test1234!", "/root");
  await rootPage.locator('a[href="/root/courses"]').click();
  await rootPage.waitForURL(/\/root\/courses/, { timeout: 10000 });
  await rootPage.waitForTimeout(2000);
  const rootText = await rootPage.locator("body").innerText();
  report.rootCoursesVisible = ["Java 수업", "멀티모달 수업", "리눅스 수업", "보안기사 수업"].every((title) =>
    rootText.includes(title),
  );
  await rootPage.screenshot({ path: path.join(outputDir, "root-courses.png"), fullPage: true });
  await rootContext.close();

  fs.writeFileSync(path.join(outputDir, "report.json"), JSON.stringify(report, null, 2));
  console.log(JSON.stringify(report, null, 2));
  await browser.close();
})();
