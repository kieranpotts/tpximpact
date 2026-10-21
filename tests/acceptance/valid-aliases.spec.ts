import { expect, test } from '@playwright/test'
import { destinationUrl, shorten, uniqueAlias } from './api.js'

// Requirement: R1
test.describe('Valid aliases', () => {
  test('The API service accepts letters, digits, hyphens, and underscores', async ({
    request,
  }) => {
    const response = await shorten(request, {
      destinationUrl,
      customAlias: uniqueAlias('Valid_Alias-09'),
    })

    expect(response.status()).toBe(201)
  })

  for (const [customAlias, code] of [
    ['ab', 'alias-invalid-length'],
    ['a'.repeat(65), 'alias-invalid-length'],
    ['has space', 'alias-invalid-characters'],
    ['dot.ted', 'alias-invalid-characters'],
    ['café', 'alias-invalid-characters'],
  ] as const) {
    test(`The API service rejects the alias "${customAlias}"`, async ({
      request,
    }) => {
      const response = await shorten(request, { destinationUrl, customAlias })

      expect(response.status()).toBe(400)
      expect(await response.json()).toMatchObject({
        errors: [{ field: 'customAlias', code }],
      })
    })
  }
})
