import { expect, test } from '@playwright/test'
import { shorten, uniqueAlias } from './api.js'

// Requirement: B4
test.describe("Reject an alias that's already taken", () => {
  test('The API service rejects a custom alias that is in use', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('taken')
    await shorten(request, {
      destinationUrl: 'https://first.example/',
      customAlias,
    })

    const response = await shorten(request, {
      destinationUrl: 'https://second.example/',
      customAlias,
    })

    expect(response.status()).toBe(400)
    expect(await response.json()).toMatchObject({
      type: 'urn:problem:url-shortener:alias-taken',
      status: 400,
    })
  })
})
