import { expect, test } from '@playwright/test'
import { publicBaseUrl } from './urls.js'

// Requirement: B12
test.describe('Reject an unsupported content type', () => {
  for (const contentType of [
    'text/plain',
    'application/x-www-form-urlencoded',
    'not a media type',
  ]) {
    test(`The API service rejects a body sent as ${contentType}`, async ({
      request,
    }) => {
      const response = await request.post(`${publicBaseUrl}/shorten`, {
        headers: { 'Content-Type': contentType },
        data: '{"destinationUrl": "https://example.com/"}',
      })

      expect(response.status()).toBe(415)
      expect(response.headers()['accept-post']).toBe('application/json')
      expect(await response.json()).toMatchObject({
        type: 'urn:problem:url-shortener:unsupported-media-type',
        status: 415,
      })
    })
  }
})
