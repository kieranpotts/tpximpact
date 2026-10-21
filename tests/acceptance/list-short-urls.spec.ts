import { expect, test } from '@playwright/test'
import { listShortUrls, shorten, uniqueAlias } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: B7
test.describe('List all short URLs', () => {
  test('The API service lists every short URL, with its alias, destination URL, and short URL', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('list')
    await shorten(request, {
      destinationUrl: 'https://custom.example/',
      customAlias,
    })
    const created = await shorten(request, {
      destinationUrl: 'https://random.example/',
    })
    const { shortUrl } = (await created.json()) as { shortUrl: string }

    const listed = await listShortUrls(request)

    expect(listed).toContainEqual({
      alias: customAlias,
      destinationUrl: 'https://custom.example/',
      shortUrl: `${publicBaseUrl}/${customAlias}`,
    })
    expect(listed).toContainEqual({
      alias: new URL(shortUrl).pathname.slice(1),
      destinationUrl: 'https://random.example/',
      shortUrl,
    })
  })

  test('The API service returns the list as JSON', async ({ request }) => {
    const response = await request.get(`${publicBaseUrl}/urls`)

    expect(response.status()).toBe(200)
    expect(response.headers()['content-type']).toMatch(/^application\/json\b/)
    expect(Array.isArray(await response.json())).toBe(true)
  })
})
