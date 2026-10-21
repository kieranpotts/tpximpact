import { expect, test } from '@playwright/test'
import { destinationUrl, shorten } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: R4
test.describe('Random aliases', () => {
  test('Random aliases are 7 letters and digits, and differ every time', async ({
    request,
  }) => {
    const aliases = new Set<string>()
    for (let i = 0; i < 20; i++) {
      const response = await shorten(request, { destinationUrl })
      const { shortUrl } = (await response.json()) as { shortUrl: string }
      const alias = shortUrl.slice(publicBaseUrl.length + 1)

      expect(alias).toMatch(/^[A-Za-z0-9]{7}$/)
      aliases.add(alias)
    }

    expect(aliases.size).toBe(20)
  })
})
