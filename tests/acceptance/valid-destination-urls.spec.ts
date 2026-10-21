import { expect, test } from '@playwright/test'
import { shorten } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: R5
test.describe('Valid destination URLs', () => {
  for (const destinationUrl of [
    'https://example.com',
    '  http://example.com/path?query=1#fragment  ',
    'https://xn--bcher-kva.example/',
  ]) {
    test(`The API service accepts "${destinationUrl}"`, async ({ request }) => {
      const response = await shorten(request, { destinationUrl })

      expect(response.status()).toBe(201)
    })
  }

  for (const [destinationUrl, code] of [
    ['', 'url-required'],
    [`https://example.com/${'a'.repeat(2_048)}`, 'url-too-long'],
    ['javascript:alert(1)', 'url-unsupported-scheme'],
    ['example.com', 'url-unsupported-scheme'],
    ['https://', 'url-invalid'],
    ['https://bücher.example/', 'url-invalid'],
    ['https://user:pass@example.com/', 'url-has-credentials'],
    [`${publicBaseUrl}/abc1234`, 'url-self-referential'],
  ] as const) {
    test(`The API service rejects "${destinationUrl.slice(0, 40)}"`, async ({
      request,
    }) => {
      const response = await shorten(request, { destinationUrl })

      expect(response.status()).toBe(400)
      expect(await response.json()).toMatchObject({
        errors: [{ field: 'destinationUrl', code }],
      })
    })
  }
})
