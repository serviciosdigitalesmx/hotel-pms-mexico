import { defineConfig, devices } from '@playwright/test';
import path from 'node:path';
import { baseURL } from './e2e-native-stack/support';

// The caller starts the integrated stack. No webServer, mocks or existing suites.
const executablePath = process.env.CHROME_EXECUTABLE_PATH;
const evidenceDir = process.env.NATIVE_E2E_OUTPUT_DIR
  ? path.resolve(process.env.NATIVE_E2E_OUTPUT_DIR)
  : path.resolve('test-results/native-stack');

export default defineConfig({
  testDir: './e2e-native-stack',
  testMatch: '**/*.spec.ts',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  forbidOnly: !!process.env.CI,
  timeout: 180_000,
  expect: { timeout: 15_000 },
  outputDir: path.join(evidenceDir, 'results'),
  // The subsequent bash gate reads both PDFs even after a passing test run.
  preserveOutput: 'always',
  reporter: [
    ['list'],
    ['html', { outputFolder: path.join(evidenceDir, 'html'), open: 'never' }],
    ['junit', { outputFile: path.join(evidenceDir, 'junit.xml') }],
  ],
  use: {
    ...devices['Desktop Chrome'],
    baseURL,
    browserName: 'chromium',
    // CI installs Playwright Chromium. System Chrome is an explicit opt-in.
    channel: executablePath ? undefined : process.env.CHROME_CHANNEL,
    launchOptions: executablePath ? { executablePath } : undefined,
    locale: 'es-MX',
    timezoneId: 'UTC',
    acceptDownloads: true,
    serviceWorkers: 'block',
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
});
