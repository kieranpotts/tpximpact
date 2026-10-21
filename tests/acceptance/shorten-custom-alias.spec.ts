import { expect, test } from '@playwright/test'
import { destinationUrl, shorten, uniqueAlias } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: B2
test.describe('Shorten a URL with a custom alias', () => {
  test('The API service returns a short URL with the custom alias', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('custom')

    const response = await shorten(request, { destinationUrl, customAlias })

    expect(response.status()).toBe(201)
    expect(await response.json()).toEqual({
      shortUrl: `${publicBaseUrl}/${customAlias}`,
    })
  })
})
