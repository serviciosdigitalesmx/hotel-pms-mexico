import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env.PLAYWRIGHT_PLATFORM_BASE_URL ?? 'http://localhost:5173';

export default defineConfig({
  testDir: './e2e-platform-monolith',
  testMatch: '**/*.spec.ts',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  forbidOnly: !!process.env.CI,
  timeout: 90_000,
  expect: { timeout: 15_000 },
  reporter: [['list'], ['junit', { outputFile: 'test-results/platform-monolith-junit.xml' }]],
  use: {
    ...devices['Desktop Chrome'],
    baseURL,
    browserName: 'chromium',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
});
