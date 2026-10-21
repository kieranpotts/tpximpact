import { defineConfig, devices } from '@playwright/test'
import { webClientUrl } from './urls.js'

export default defineConfig({
  testDir: '.',
  testMatch: '*.spec.ts',
  forbidOnly: process.env.CI !== undefined,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: webClientUrl,
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
