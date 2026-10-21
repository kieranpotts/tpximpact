import { expect, test } from '@playwright/test'
import { destinationUrl, shorten } from './api.js'

// Requirement: R2
test.describe('Reserved aliases', () => {
  for (const customAlias of ['shorten', 'URLS', 'Api', 'health', 'v10', 'V2026']) {
    test(`The API service rejects the reserved alias "${customAlias}"`, async ({
      request,
    }) => {
      const response = await shorten(request, { destinationUrl, customAlias })

      expect(response.status()).toBe(400)
      expect(await response.json()).toMatchObject({
        errors: [{ field: 'customAlias', code: 'alias-reserved' }],
      })
    })
  }
})
