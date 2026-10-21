import { expect, test } from '@playwright/test'
import { destinationUrl, listShortUrls, shorten, uniqueAlias } from './api.js'

// Requirement: R11
test.describe('List order', () => {
  test('The API service lists short URLs newest first', async ({
    request,
  }) => {
    const created = [
      uniqueAlias('first'),
      uniqueAlias('second'),
      uniqueAlias('third'),
    ]
    for (const customAlias of created) {
      await shorten(request, { destinationUrl, customAlias })
    }

    // Other tests may add short URLs in between, so only the relative order
    // of these ones is checked.
    const aliases = (await listShortUrls(request))
      .map((item) => item.alias)
      .filter((alias) => created.includes(alias))

    expect(aliases).toEqual([...created].reverse())
  })
})
