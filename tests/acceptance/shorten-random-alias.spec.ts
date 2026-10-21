import { expect, test } from '@playwright/test'
import { destinationUrl, shorten } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: B1
test.describe('Shorten a URL with a random alias', () => {
  test('The API service returns a short URL with a random alias', async ({
    request,
  }) => {
    const response = await shorten(request, { destinationUrl })

    expect(response.status()).toBe(201)
    expect(response.headers()['content-type']).toBe('application/json')
    const { shortUrl } = (await response.json()) as { shortUrl: string }
    expect(shortUrl.startsWith(`${publicBaseUrl}/`)).toBe(true)
  })
})
