import { expect, test } from '@playwright/test'
import { publicBaseUrl } from './urls.js'

// Requirement: B15
test.describe('Welcome page', () => {
  test('The API service greets visitors to its root path, with an HTML page', async ({
    request,
  }) => {
    const response = await request.get(`${publicBaseUrl}/`)

    expect(response.status()).toBe(200)
    expect(response.headers()['content-type']).toMatch(/^text\/html\b/)
    expect(await response.text()).toContain('Welcome to the URL shortener')
  })
})
