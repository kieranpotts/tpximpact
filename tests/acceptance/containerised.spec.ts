import { expect, test } from '@playwright/test'
import { publicBaseUrl } from './urls.js'

// Requirement: Q4
test.describe('Containerised', () => {
  test('The web client serves its home page', async ({ page }) => {
    await page.goto('/')

    await expect(page.getByRole('heading', { level: 1 })).toHaveText(
      'Shorten a URL'
    )
  })

  test('The API service is reachable at the public base URL', async ({
    request,
  }) => {
    const response = await request.get(`${publicBaseUrl}/openapi.yaml`)

    expect(response.status()).toBe(200)
    expect(response.headers()['x-request-id']).toBeTruthy()
  })
})
