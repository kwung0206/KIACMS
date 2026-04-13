const { chromium } = require("playwright");
(async () => {
  const browser = await chromium.launch({ channel: "msedge", headless: true });
  const context = await browser.newContext({ locale: "ko-KR", timezoneId: "Asia/Seoul" });
  const page = await context.newPage();
  page.on('response', async (response) => {
    if (response.url().includes('/api/users/me')) {
      console.log('USER_ME', response.status(), response.url());
      try {
        console.log(await response.text());
      } catch (error) {
        console.log('response text failed');
      }
    }
  });
  await page.goto("http://localhost:5173/login", { waitUntil: "networkidle" });
  await page.locator('input[name="email"]').fill('student.render@kiacms.local');
  await page.locator('input[name="password"]').fill('Test1234!');
  await page.locator('button[type="submit"]').click();
  await page.waitForURL(/\/student/, { timeout: 15000 });
  await page.goto('http://localhost:5173/me', { waitUntil: 'networkidle' });
  console.log('FINAL_URL=' + page.url());
  await browser.close();
})();
