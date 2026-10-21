import { expect, test } from '@playwright/test'
import { shorten } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: B3
test.describe('Reject invalid input', () => {
  test('The API service says which fields are invalid, and why', async ({
    request,
  }) => {
    const response = await shorten(request, {
      destinationUrl: 'ftp://example.com/',
      customAlias: 'ab',
    })

    expect(response.status()).toBe(400)
    expect(response.headers()['content-type']).toBe('application/problem+json')
    expect(await response.json()).toMatchObject({
      type: 'urn:problem:url-shortener:validation-failed',
      status: 400,
      errors: [
        { field: 'destinationUrl', code: 'url-unsupported-scheme' },
        { field: 'customAlias', code: 'alias-invalid-length' },
      ],
    })
  })

  test('The API service rejects a body that is not valid JSON', async ({
    request,
  }) => {
    const response = await request.post(`${publicBaseUrl}/shorten`, {
      headers: { 'Content-Type': 'application/json' },
      data: '{"destinationUrl": ',
    })

    expect(response.status()).toBe(400)
    expect(await response.json()).toMatchObject({
      type: 'urn:problem:url-shortener:malformed-request',
    })
  })
})
