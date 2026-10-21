import { expect, test } from '@playwright/test'
import { follow, shorten, uniqueAlias } from './api.js'
import { compose, startAndWait } from './compose.js'

// Requirement: B8
test.describe('Persist across restarts', () => {
  test.setTimeout(120_000)

  test('Every alias survives a restart of the API service and its database', async ({
    request,
  }) => {
    const created = new Map<string, string>()
    const customAlias = uniqueAlias('persist')
    created.set(customAlias, 'https://custom.example/')
    await shorten(request, {
      destinationUrl: 'https://custom.example/',
      customAlias,
    })
    const response = await shorten(request, {
      destinationUrl: 'https://random.example/',
    })
    const { shortUrl } = (await response.json()) as { shortUrl: string }
    created.set(new URL(shortUrl).pathname.slice(1), 'https://random.example/')

    compose('stop', 'api', 'db')
    startAndWait('db', 'api')

    for (const [alias, destinationUrl] of created) {
      const redirect = await follow(request, alias)

      expect(redirect.status(), alias).toBe(302)
      expect(redirect.headers().location, alias).toBe(destinationUrl)
    }
  })
})
