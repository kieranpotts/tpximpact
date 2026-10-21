import { expect, test } from '@playwright/test'
import { publicBaseUrl } from './urls.js'

// Requirement: B13
test.describe('Reject an oversized request', () => {
  test('The API service rejects a body larger than 16 KiB', async ({
    request,
  }) => {
    const json = '{"destinationUrl": "https://example.com/"}'
    const body = json.slice(0, -1) + ' '.repeat(16 * 1024 - json.length + 1) + '}'

    const response = await request.post(`${publicBaseUrl}/shorten`, {
      headers: { 'Content-Type': 'application/json' },
      data: body,
    })

    expect(response.status()).toBe(413)
    expect(await response.json()).toMatchObject({
      type: 'urn:problem:url-shortener:payload-too-large',
      status: 413,
    })
  })
})
