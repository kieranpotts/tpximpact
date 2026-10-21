import { expect, test } from '@playwright/test'
import { destinationUrl, shorten } from './api.js'

// Requirement: R8
test.describe('No deduplication', () => {
  test('Shortening the same URL twice makes two short URLs', async ({
    request,
  }) => {
    const first = await shorten(request, { destinationUrl })
    const second = await shorten(request, { destinationUrl })

    expect(first.status()).toBe(201)
    expect(second.status()).toBe(201)
    expect(await first.json()).not.toEqual(await second.json())
  })
})
