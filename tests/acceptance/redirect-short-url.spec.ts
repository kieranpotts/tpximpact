import { expect, test } from '@playwright/test'
import { destinationUrl, follow, shorten, uniqueAlias } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: B5
test.describe('Redirect a short URL', () => {
  test('The API service redirects an alias to its destination URL', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('redirect')
    await shorten(request, { destinationUrl, customAlias })

    const response = await follow(request, customAlias)

    expect(response.status()).toBe(302)
    expect(response.headers().location).toBe(destinationUrl)
    expect(response.headers()['cache-control']).toBe('no-store')
  })

  test('The API service redirects a random alias too', async ({
    request,
  }) => {
    const created = await shorten(request, { destinationUrl })
    const { shortUrl } = (await created.json()) as { shortUrl: string }

    const response = await request.get(shortUrl, { maxRedirects: 0 })

    expect(response.status()).toBe(302)
    expect(response.headers().location).toBe(destinationUrl)
  })

  test('The API service returns not found for an alias that does not exist', async ({
    request,
  }) => {
    const response = await follow(request, uniqueAlias('missing'))

    expect(response.status()).toBe(404)
    expect(await response.json()).toMatchObject({
      type: 'urn:problem:url-shortener:not-found',
      status: 404,
    })
  })

  test('The API service returns not found for what cannot be an alias', async ({
    request,
  }) => {
    for (const path of ['ab', 'not.an.alias', 'shorten', 'v1']) {
      const response = await follow(request, path)

      expect(response.status(), path).toBe(404)
    }
  })

  test('A browser shows a page for an alias that does not exist', async ({
    page,
  }) => {
    const response = await page.goto(
      `${publicBaseUrl}/${uniqueAlias('missing')}`
    )

    expect(response?.status()).toBe(404)
    await expect(page.getByRole('heading', { level: 1 })).toHaveText(
      'Link not found'
    )
  })
})
