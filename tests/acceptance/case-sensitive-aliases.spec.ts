import { expect, test } from '@playwright/test'
import { destinationUrl, shorten, uniqueAlias } from './api.js'

// Requirement: R3
test.describe('Aliases are case-sensitive', () => {
  test('Aliases that differ only in case are different aliases', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('MyLink')

    const upper = await shorten(request, { destinationUrl, customAlias })
    const lower = await shorten(request, {
      destinationUrl,
      customAlias: customAlias.toLowerCase(),
    })

    expect(upper.status()).toBe(201)
    expect(lower.status()).toBe(201)
  })
})
