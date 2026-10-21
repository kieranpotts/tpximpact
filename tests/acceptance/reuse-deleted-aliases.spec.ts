import { expect, test } from '@playwright/test'
import { deleteShortUrl, follow, shorten, uniqueAlias } from './api.js'

// Requirement: R10
test.describe('Deleted aliases are free to reuse', () => {
  test('An alias can be created again, straight after it is deleted', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('reuse')
    await shorten(request, {
      destinationUrl: 'https://first.example/',
      customAlias,
    })
    await deleteShortUrl(request, customAlias)

    const response = await shorten(request, {
      destinationUrl: 'https://second.example/',
      customAlias,
    })

    expect(response.status()).toBe(201)
    const redirect = await follow(request, customAlias)
    expect(redirect.headers().location).toBe('https://second.example/')
  })
})
