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
  await page.goto('http://localhost:5173/me', { waitUntil: 'networkidle' });
  console.log('URL=' + page.url());
  console.log(await page.locator('body').innerText());
  await page.screenshot({ path: 'C:/Users/kgj01/Documents/KIACMS/artifacts/ui-check/me-debug.png', fullPage: true });
  await browser.close();
})();
