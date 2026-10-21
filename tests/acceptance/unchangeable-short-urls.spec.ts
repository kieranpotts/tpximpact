import { expect, test } from '@playwright/test'
import { follow, shorten, uniqueAlias } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: R9
test.describe("Short URLs can't be changed", () => {
  test('Shortening an existing alias again does not change where it redirects', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('fixed')
    await shorten(request, {
      destinationUrl: 'https://first.example/',
      customAlias,
    })

    const response = await shorten(request, {
      destinationUrl: 'https://second.example/',
      customAlias,
    })

    expect(response.status()).toBe(400)
    const redirect = await follow(request, customAlias)
    expect(redirect.headers().location).toBe('https://first.example/')
  })

  test('The API service offers no way to change a short URL', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('fixed')
    await shorten(request, {
      destinationUrl: 'https://first.example/',
      customAlias,
    })
    const data = { destinationUrl: 'https://second.example/' }

    for (const response of [
      await request.put(`${publicBaseUrl}/${customAlias}`, { data }),
      await request.patch(`${publicBaseUrl}/${customAlias}`, { data }),
    ]) {
      expect(response.status()).toBeGreaterThanOrEqual(400)
      expect(response.status()).toBeLessThan(500)
    }
    const redirect = await follow(request, customAlias)
    expect(redirect.headers().location).toBe('https://first.example/')
  })
})
