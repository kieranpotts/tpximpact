import { expect, test } from '@playwright/test'
import { follow, shorten, uniqueAlias } from './api.js'

// Requirement: R7
test.describe('Destination URLs are stored as given', () => {
  test('The API service redirects to the trimmed URL, otherwise unchanged', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('as-given')
    const unusual = 'HTTPS://Example.COM:443/a/../b?x=%7E&y=1#Top'
    await shorten(request, {
      destinationUrl: `  ${unusual}\n`,
      customAlias,
    })

    const response = await follow(request, customAlias)

    expect(response.headers().location).toBe(unusual)
  })
})
