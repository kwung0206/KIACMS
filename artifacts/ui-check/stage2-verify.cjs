const fs = require("fs");
const path = require("path");
const { chromium } = require("playwright");

const baseUrl = "http://localhost:5173";
const outputDir = "C:/Users/kgj01/Documents/KIACMS/artifacts/ui-check/stage2";
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

async function openChatbot(page) {
  await page.getByRole("button", { name: "AI 도우미" }).click();
  await page.locator(".chatbot-drawer").waitFor({ timeout: 10000 });
}

async function closeChatbot(page) {
  await page.locator(".chatbot-drawer").getByRole("button", { name: "닫기" }).click();
  await page.locator(".chatbot-drawer").waitFor({ state: "hidden", timeout: 10000 });
}

async function askChatbot(page, message, options = {}) {
  const useQuickPrompt = options.useQuickPrompt ?? false;
  const assistantMessages = page.locator(".chatbot-message-assistant");
  const beforeCount = await assistantMessages.count();

  if (useQuickPrompt) {
    await page.locator(".chatbot-quick-panel").getByRole("button", { name: message, exact: true }).click();
  } else {
    await page.locator(".chatbot-form textarea").fill(message);
    await page.locator(".chatbot-form").getByRole("button", { name: /전송|답변 생성 중/ }).click();
  }

  await page.waitForFunction(
    (previousCount) => {
      const items = document.querySelectorAll(".chatbot-message-assistant");
      if (items.length <= previousCount) {
        return false;
      }

      const last = items[items.length - 1];
      return !last.textContent.includes("답변을 준비하고 있습니다...");
    },
    beforeCount,
    { timeout: 30000 },
  );

  const lastAssistant = assistantMessages.last();
  const content = (await lastAssistant.locator("p").first().innerText()).trim();
  const links = await lastAssistant.locator(".chatbot-section .chatbot-action-button").allInnerTexts();
  const courseCards = await lastAssistant.locator(".chatbot-section .chatbot-recommendation-card").allInnerTexts();
  return {
    content,
    links: links.map((value) => value.trim()).filter(Boolean),
    courseCards: courseCards.map((value) => value.trim()).filter(Boolean),
    element: lastAssistant,
  };
}

async function createProjectViaUi(page, title) {
  await page.locator(".sidebar-nav").getByRole("link", { name: "프로젝트" }).click();
  await page.locator(".page-header h1").filter({ hasText: "프로젝트 게시판" }).waitFor({ timeout: 15000 });
  await page.getByRole("link", { name: "새 모집글 작성" }).click();
  await page.locator(".page-header h1").filter({ hasText: "새 모집글 작성" }).waitFor({ timeout: 15000 });

  await page.getByLabel("프로젝트 제목").fill(title);
  await page.getByLabel("프로젝트 소개").fill("AI 도우미와 강좌 추천, 프로젝트 알림 기능을 함께 검증하는 서비스형 프로젝트입니다.");
  await page.getByLabel("목표").fill("학생이 챗봇으로 진로 상담을 받고 프로젝트 추천 알림까지 받는 흐름을 구현합니다.");
  await page.getByLabel("기술 스택").fill("React, Spring Boot, PostgreSQL, OpenAI");
  await page.getByLabel("예상 진행 기간").fill("6주");
  await page.getByLabel("연락처").fill("student@kiacms.local");
  await page.getByLabel("PM 소개").fill("학생 계정이 직접 운영하는 2차 AI 기능 검증용 모집글입니다.");
  await page.getByLabel("PM 경험 및 강점").fill("Spring Boot 기반 백엔드 개발과 서비스형 UI 흐름 정리에 익숙합니다.");
  await page.getByLabel("포지션명").first().fill("백엔드");
  await page.getByLabel("포지션 설명").first().fill("Spring Boot API와 AI 연동 로직을 담당합니다.");
  await page.getByLabel("필수 역량").first().fill("Java, Spring Boot, PostgreSQL");

  await page.screenshot({
    path: path.join(outputDir, "student-project-create-stage2.png"),
    fullPage: true,
  });

  await page.getByRole("button", { name: "모집글 등록" }).click();
  await page.waitForURL(/http:\/\/localhost:5173\/projects\/[^/]+$/, { timeout: 20000 });
  await page.waitForLoadState("networkidle");
  return page.url();
}

async function verifyNotification(page, expectedProjectTitle) {
  await page.locator(".sidebar-nav").getByRole("link", { name: "알림" }).click();
  await page.locator(".page-header h1").filter({ hasText: "알림" }).waitFor({ timeout: 15000 });
  const notificationCard = page
    .locator("article.info-card")
    .filter({ hasText: "AI 프로젝트 추천" })
    .filter({ hasText: expectedProjectTitle })
    .first();
  await notificationCard.waitFor({ timeout: 20000 });
  return {
    title: (await notificationCard.locator("strong").first().innerText()).trim(),
    message: (await notificationCard.locator("span").first().innerText()).trim(),
  };
}

(async () => {
  const browser = await chromium.launch({
    channel: "msedge",
    headless: true,
  });

  const report = {
    screenshots: {},
    chatbot: {},
    projectAi: {},
    notifications: {},
    accounts: {},
  };

  const stamp = uniqueStamp();
  const projectTitle = `Stage2 AI Service Project ${stamp}`;

  try {
    const studentContext = await createContext(browser);
    const studentPage = await studentContext.newPage();
    await login(studentPage, "student@kiacms.local", "Test1234!", /\/student$/);
    report.accounts.student = {
      topbar: (await studentPage.locator(".topbar-user").innerText()).trim(),
    };

    await openChatbot(studentPage);
    await studentPage.screenshot({
      path: path.join(outputDir, "student-chatbot-open.png"),
      fullPage: true,
    });
    report.screenshots.studentChatbotOpen = path.join(outputDir, "student-chatbot-open.png");

    const supportGuide = await askChatbot(studentPage, "이 사이트에서 지원서는 어디서 수정하나요?", {
      useQuickPrompt: true,
    });
    report.chatbot.studentSupportGuide = {
      content: supportGuide.content,
      links: supportGuide.links,
    };
    if (!supportGuide.content.includes("지원서") || !supportGuide.links.some((value) => value.includes("내 지원 현황"))) {
      throw new Error("학생 지원서 안내 응답이 기대와 다릅니다.");
    }

    const myPostsGuide = await askChatbot(studentPage, "내 모집글은 어디서 볼 수 있나요?", {
      useQuickPrompt: true,
    });
    report.chatbot.studentMyPostsGuide = {
      content: myPostsGuide.content,
      links: myPostsGuide.links,
    };
    if (!myPostsGuide.links.some((value) => value.includes("내 모집글"))) {
      throw new Error("내 모집글 안내 링크가 챗봇 응답에 없습니다.");
    }

    const backendCareer = await askChatbot(studentPage, "저는 백엔드 개발자가 되고 싶은데 어떤 강좌를 들으면 좋을까요?", {
      useQuickPrompt: true,
    });
    report.chatbot.studentCareerBackend = {
      content: backendCareer.content,
      courseCards: backendCareer.courseCards,
    };
    if (backendCareer.courseCards.length === 0) {
      throw new Error("백엔드 진로 질문에서 추천 강좌 카드가 표시되지 않았습니다.");
    }

    const securityCareer = await askChatbot(studentPage, "저는 정보보안 쪽 진로를 생각 중인데 추천 수업이 있나요?");
    report.chatbot.studentCareerSecurity = {
      content: securityCareer.content,
      courseCards: securityCareer.courseCards,
    };
    if (securityCareer.courseCards.length === 0) {
      throw new Error("보안 진로 질문에서 추천 강좌 카드가 표시되지 않았습니다.");
    }

    await studentPage.route("**/api/ai/chatbot/messages", async (route) => {
      await route.abort();
      await studentPage.unroute("**/api/ai/chatbot/messages");
    });
    await studentPage.locator(".chatbot-form textarea").fill("오류 처리도 확인해 주세요.");
    await studentPage.locator(".chatbot-form").getByRole("button", { name: "전송" }).click();
    await studentPage.locator(".chatbot-drawer .form-alert.error").waitFor({ timeout: 15000 });
    report.chatbot.studentError = {
      message: (await studentPage.locator(".chatbot-drawer .form-alert.error").innerText()).trim(),
    };

    await studentPage.screenshot({
      path: path.join(outputDir, "student-chatbot-conversations.png"),
      fullPage: true,
    });
    report.screenshots.studentChatbotConversations = path.join(outputDir, "student-chatbot-conversations.png");
    await closeChatbot(studentPage);

    const projectUrl = await createProjectViaUi(studentPage, projectTitle);
    report.projectAi.createdProjectUrl = projectUrl;
    report.projectAi.createdProjectTitle = projectTitle;

    const aiPanel = studentPage.locator("section.panel").filter({ hasText: "AI 프로젝트 분석" }).first();
    await aiPanel.waitFor({ timeout: 15000 });
    await studentPage.getByRole("button", { name: "AI 분석 실행" }).click();
    await studentPage.waitForFunction(
      () => document.querySelectorAll(".chatbot-recommendation-card").length > 0,
      { timeout: 45000 },
    );
    report.projectAi.analysis = {
      summary: (await aiPanel.locator(".info-card").first().innerText()).trim(),
      courseCount: await aiPanel.locator(".chatbot-recommendation-card").count(),
    };
    if (report.projectAi.analysis.courseCount < 1) {
      throw new Error("프로젝트 AI 분석에서 추천 카드가 표시되지 않았습니다.");
    }

    await studentPage.screenshot({
      path: path.join(outputDir, "student-project-ai-analysis.png"),
      fullPage: true,
    });
    report.screenshots.projectAiAnalysis = path.join(outputDir, "student-project-ai-analysis.png");

    await studentPage.getByRole("button", { name: "추천 알림 보내기" }).click();
    await studentPage.waitForFunction(
      () => {
        const cards = Array.from(document.querySelectorAll(".info-card"));
        return cards.some((card) => card.textContent.includes("전송 1건"));
      },
      { timeout: 45000 },
    );
    const matchedInfo = (await aiPanel.locator(".info-card").last().innerText()).trim();
    report.projectAi.notificationDispatch = {
      message: (await studentPage.locator(".form-alert").last().innerText()).trim(),
      matchedInfo,
    };
    if (!matchedInfo.includes("전송 1건")) {
      throw new Error("프로젝트 추천 알림 전송 결과가 기대와 다릅니다.");
    }
    await studentPage.screenshot({
      path: path.join(outputDir, "student-project-ai-notify.png"),
      fullPage: true,
    });
    report.screenshots.projectAiNotify = path.join(outputDir, "student-project-ai-notify.png");
    await studentContext.close();

    const rootContext = await createContext(browser);
    const rootPage = await rootContext.newPage();
    await login(rootPage, "root@kiacms.local", "Test1234!", /\/root$/);
    report.accounts.root = {
      topbar: (await rootPage.locator(".topbar-user").innerText()).trim(),
    };
    await openChatbot(rootPage);
    const rootGuide = await askChatbot(rootPage, "관리자 승인은 어디서 하나요?", { useQuickPrompt: true });
    report.chatbot.rootGuide = {
      content: rootGuide.content,
      links: rootGuide.links,
    };
    if (!rootGuide.content.includes("Root") || !rootGuide.links.some((value) => value.includes("Root 운영 홈"))) {
      throw new Error("Root 챗봇 안내가 기대와 다릅니다.");
    }
    await rootPage.screenshot({
      path: path.join(outputDir, "root-chatbot.png"),
      fullPage: true,
    });
    report.screenshots.rootChatbot = path.join(outputDir, "root-chatbot.png");
    await rootContext.close();

    const teacherContext = await createContext(browser);
    const teacherPage = await teacherContext.newPage();
    await login(teacherPage, "teacher@kiacms.local", "Test1234!", /\/instructor$/);
    report.accounts.teacher = {
      topbar: (await teacherPage.locator(".topbar-user").innerText()).trim(),
    };
    await openChatbot(teacherPage);
    const teacherGuide = await askChatbot(teacherPage, "태그된 정리글은 어디서 확인하나요?", { useQuickPrompt: true });
    report.chatbot.teacherGuide = {
      content: teacherGuide.content,
      links: teacherGuide.links,
    };
    if (!teacherGuide.links.some((value) => value.includes("태그된 정리글"))) {
      throw new Error("강사 챗봇 안내가 기대와 다릅니다.");
    }
    await teacherPage.screenshot({
      path: path.join(outputDir, "teacher-chatbot.png"),
      fullPage: true,
    });
    report.screenshots.teacherChatbot = path.join(outputDir, "teacher-chatbot.png");
    await teacherContext.close();

    const mentorContext = await createContext(browser);
    const mentorPage = await mentorContext.newPage();
    await login(mentorPage, "mentor@kiacms.local", "Test1234!", /\/mentor$/);
    report.accounts.mentor = {
      topbar: (await mentorPage.locator(".topbar-user").innerText()).trim(),
    };
    await openChatbot(mentorPage);
    const mentorGuide = await askChatbot(mentorPage, "멘토는 어떤 기능을 사용할 수 있나요?", { useQuickPrompt: true });
    report.chatbot.mentorGuide = {
      content: mentorGuide.content,
      links: mentorGuide.links,
    };
    if (!mentorGuide.content.includes("수강생") || !mentorGuide.links.some((value) => value.includes("관리 학생"))) {
      throw new Error("멘토 챗봇 안내가 기대와 다릅니다.");
    }
    await mentorPage.screenshot({
      path: path.join(outputDir, "mentor-chatbot.png"),
      fullPage: true,
    });
    report.screenshots.mentorChatbot = path.join(outputDir, "mentor-chatbot.png");
    await mentorContext.close();

    const notifiedStudentContext = await createContext(browser);
    const notifiedStudentPage = await notifiedStudentContext.newPage();
    await login(notifiedStudentPage, "stage1-20260413214720@kiacms.local", "Test1234!", /\/student$/);
    report.accounts.notifiedStudent = {
      topbar: (await notifiedStudentPage.locator(".topbar-user").innerText()).trim(),
    };
    const notificationResult = await verifyNotification(notifiedStudentPage, projectTitle);
    report.notifications.projectAi = notificationResult;
    await notifiedStudentPage.screenshot({
      path: path.join(outputDir, "project-ai-notification-received.png"),
      fullPage: true,
    });
    report.screenshots.projectAiNotification = path.join(outputDir, "project-ai-notification-received.png");
    await notifiedStudentContext.close();
  } catch (error) {
    report.error = {
      message: error.message,
      stack: error.stack,
    };
  } finally {
    await browser.close();
    fs.writeFileSync(path.join(outputDir, "report.json"), JSON.stringify(report, null, 2), "utf8");
    console.log(JSON.stringify(report, null, 2));
  }
})();
