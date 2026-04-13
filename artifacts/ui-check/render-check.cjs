const fs = require("fs");
const path = require("path");
const { chromium } = require("playwright");

const baseUrl = "http://localhost:5173";
const screenshotDir = "C:/Users/kgj01/Documents/KIACMS/artifacts/ui-check";
fs.mkdirSync(screenshotDir, { recursive: true });

async function login(page, email, password) {
  await page.goto(`${baseUrl}/login`, { waitUntil: "networkidle" });
  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="password"]').fill(password);
  await page.getByRole("button", { name: "로그인" }).click();
}

(async () => {
  const browser = await chromium.launch({
    channel: "msedge",
    headless: true,
  });

  const report = {};

  const rootContext = await browser.newContext({
    viewport: { width: 1440, height: 1100 },
    locale: "ko-KR",
    timezoneId: "Asia/Seoul",
  });
  const rootPage = await rootContext.newPage();
  await login(rootPage, "root.render@kiacms.local", "Root1234!");
  await rootPage.waitForURL(/\/root/, { timeout: 15000 });
  await rootPage.waitForLoadState("networkidle");
  report.rootTopbarLabel = (await rootPage.locator(".topbar-user").innerText()).trim();
  await rootPage.screenshot({
    path: path.join(screenshotDir, "root-home.png"),
    fullPage: true,
  });
  await rootContext.close();

  const studentContext = await browser.newContext({
    viewport: { width: 1440, height: 1200 },
    locale: "ko-KR",
    timezoneId: "Asia/Seoul",
  });
  const studentPage = await studentContext.newPage();
  await login(studentPage, "student.render@kiacms.local", "Test1234!");
  await studentPage.waitForURL(/\/student/, { timeout: 15000 });
  await studentPage.waitForLoadState("networkidle");

  report.studentTopbarLabel = (await studentPage.locator(".topbar-user").innerText()).trim();

  await studentPage.getByRole("link", { name: "마이페이지" }).click();
  await studentPage.waitForURL(/\/me/, { timeout: 10000 });
  await studentPage.getByRole("heading", { name: "회원정보 수정" }).waitFor({ timeout: 10000 });
  await studentPage.locator('input[name="name"]').fill("렌더링 학생 수정");
  await studentPage.locator('input[name="phoneNumber"]').fill("010-7777-8888");
  await studentPage.locator('textarea[name="bio"]').fill("회원정보 수정 렌더링 검증용 소개입니다.");
  await studentPage.getByRole("button", { name: "회원정보 저장" }).click();
  await studentPage.getByText("회원정보가 저장되었습니다.").waitFor({ timeout: 10000 });
  report.profileSaveMessage = "회원정보가 저장되었습니다.";
  await studentPage.screenshot({
    path: path.join(screenshotDir, "mypage.png"),
    fullPage: true,
  });

  await studentPage.getByRole("button", { name: "로그아웃" }).click();
  await studentPage.getByRole("dialog").waitFor({ timeout: 5000 });
  report.logoutModalTitle = (await studentPage.getByRole("heading", { name: "로그아웃" }).innerText()).trim();
  report.logoutModalBody = (await studentPage.getByText("로그아웃 하시겠습니까?").innerText()).trim();
  await studentPage.screenshot({
    path: path.join(screenshotDir, "logout-modal.png"),
    fullPage: true,
  });
  await studentPage.getByRole("button", { name: "취소" }).click();

  await studentPage.getByRole("link", { name: "프로젝트" }).click();
  await studentPage.waitForURL(/\/projects$/, { timeout: 10000 });
  await studentPage.getByRole("link", { name: "내 모집글" }).click();
  await studentPage.waitForURL(/\/student\/projects\/me/, { timeout: 10000 });
  await studentPage.getByRole("heading", { name: "내 모집글" }).waitFor({ timeout: 10000 });
  const boardButton = studentPage.getByRole("link", { name: "게시판 보기" }).first();
  const newPostButton = studentPage.getByRole("link", { name: "새 모집글 작성" }).first();
  const boardBox = await boardButton.boundingBox();
  const newPostBox = await newPostButton.boundingBox();
  report.projectButtons = {
    board: {
      width: Math.round(boardBox.width),
      height: Math.round(boardBox.height),
      y: Math.round(boardBox.y),
    },
    newPost: {
      width: Math.round(newPostBox.width),
      height: Math.round(newPostBox.height),
      y: Math.round(newPostBox.y),
    },
  };
  await studentPage.screenshot({
    path: path.join(screenshotDir, "student-projects-me.png"),
    fullPage: true,
  });

  await studentPage.getByRole("link", { name: "내 노트" }).click();
  await studentPage.waitForURL(/\/student\/notes$/, { timeout: 10000 });
  await studentPage.getByRole("main").getByRole("heading", { name: "내 노트" }).waitFor({ timeout: 10000 });
  const noteEmptyTitle = (await studentPage.locator(".empty-state h3").innerText()).trim();
  const koreaToday = await studentPage.evaluate(
    () => new Intl.DateTimeFormat("ko-KR", { timeZone: "Asia/Seoul", dateStyle: "medium" }).format(new Date()),
  );
  report.notesEmptyTitle = noteEmptyTitle;
  report.koreaToday = koreaToday;
  report.notesUsesKoreaToday = noteEmptyTitle.includes(koreaToday);
  await studentPage.screenshot({
    path: path.join(screenshotDir, "student-notes.png"),
    fullPage: true,
  });

  await browser.close();
  fs.writeFileSync(path.join(screenshotDir, "report.json"), JSON.stringify(report, null, 2));
  console.log(JSON.stringify(report, null, 2));
})();
