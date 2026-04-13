const { chromium } = require("playwright");
(async () => {
  const browser = await chromium.launch({ channel: "msedge", headless: true });
  const context = await browser.newContext({ locale: "ko-KR", timezoneId: "Asia/Seoul" });
  const page = await context.newPage();
  await page.goto("http://localhost:5173/login", { waitUntil: "networkidle" });
  await page.locator('input[name="email"]').fill('student.render@kiacms.local');
  await page.locator('input[name="password"]').fill('Test1234!');
  await page.locator('button[type="submit"]').click();
  await page.waitForURL(/\/student/, { timeout: 15000 });
  const storage = await page.evaluate(() => ({ token: localStorage.getItem('kiacms.accessToken'), user: localStorage.getItem('kiacms.user') }));
  console.log(JSON.stringify(storage, null, 2));
  await browser.close();
})();
