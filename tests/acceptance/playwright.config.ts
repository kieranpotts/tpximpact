import { defineConfig, devices } from '@playwright/test'
import { webClientUrl } from './urls.js'

/**
 * Tests that stop and start the system's containers. They run one at a time,
 * after every other test, so they can't disrupt them.
 */
const disruptive = [
  'persist-across-restarts.spec.ts',
  'database-outage.spec.ts',
]

export default defineConfig({
  testDir: '.',
  testMatch: '*.spec.ts',
  forbidOnly: process.env.CI !== undefined,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: webClientUrl,
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      testIgnore: disruptive,
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'disruptive',
      testMatch: disruptive,
      dependencies: ['chromium'],
      workers: 1,
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
