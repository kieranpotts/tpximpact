import { expect, test } from '@playwright/test'
import { destinationUrl, shorten } from './api.js'

// Requirement: R6
test.describe('An empty custom alias is invalid', () => {
  test('The API service rejects an empty custom alias', async ({
    request,
  }) => {
    const response = await shorten(request, { destinationUrl, customAlias: '' })

    expect(response.status()).toBe(400)
    expect(await response.json()).toMatchObject({
      errors: [{ field: 'customAlias', code: 'alias-invalid-length' }],
    })
  })

  test('The API service makes a random alias when the custom alias is null', async ({
    request,
  }) => {
    const response = await shorten(request, {
      destinationUrl,
      customAlias: null,
    })

    expect(response.status()).toBe(201)
  })
})
