import { expect, test } from '@playwright/test'
import {
  deleteShortUrl,
  destinationUrl,
  follow,
  listShortUrls,
  shorten,
  uniqueAlias,
} from './api.js'

// Requirement: B6
test.describe('Delete a short URL', () => {
  test('The API service deletes a short URL, which then stops redirecting', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('delete')
    await shorten(request, { destinationUrl, customAlias })

    const response = await deleteShortUrl(request, customAlias)

    expect(response.status()).toBe(204)
    expect(await response.text()).toBe('')
    expect((await follow(request, customAlias)).status()).toBe(404)
    const aliases = (await listShortUrls(request)).map((item) => item.alias)
    expect(aliases).not.toContain(customAlias)
  })

  test('The API service returns not found for an alias that does not exist', async ({
    request,
  }) => {
    const response = await deleteShortUrl(request, uniqueAlias('missing'))

    expect(response.status()).toBe(404)
    expect(await response.json()).toMatchObject({
      type: 'urn:problem:url-shortener:not-found',
      status: 404,
    })
  })

  test('The API service returns not found for an alias that is already deleted', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('delete')
    await shorten(request, { destinationUrl, customAlias })
    await deleteShortUrl(request, customAlias)

    const response = await deleteShortUrl(request, customAlias)

    expect(response.status()).toBe(404)
  })

  test('The API service returns not found for what cannot be an alias', async ({
    request,
  }) => {
    for (const path of ['ab', 'not.an.alias', 'shorten', 'urls', 'v1']) {
      const response = await deleteShortUrl(request, path)

      expect(response.status(), path).toBe(404)
    }
  })
})
