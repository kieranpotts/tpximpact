import { expect, test } from '@playwright/test'
import {
  deleteShortUrl,
  destinationUrl,
  follow,
  shorten,
  uniqueAlias,
} from './api.js'
import { compose, startAndWait } from './compose.js'
import { publicBaseUrl } from './urls.js'

// Requirement: Q7
test.describe('Survives a database outage', () => {
  test.setTimeout(120_000)

  // Restarts the database even if the test fails, so it can't break the
  // tests that follow.
  test.afterEach(() => {
    startAndWait('db')
  })

  test('Requests that need the database fail cleanly while it is down, and work again once it is back', async ({
    request,
  }) => {
    const customAlias = uniqueAlias('outage')
    await shorten(request, { destinationUrl, customAlias })

    compose('stop', 'db')

    const started = Date.now()
    const failed = await follow(request, customAlias)
    const body = await failed.text()

    expect(Date.now() - started).toBeLessThan(5_000)
    expect(failed.status()).toBe(503)
    expect(failed.headers()['content-type']).toMatch(
      /^application\/problem\+json\b/
    )
    expect(JSON.parse(body)).toMatchObject({
      type: 'urn:problem:url-shortener:service-unavailable',
      status: 503,
    })
    expect(body).not.toMatch(/exception|\bat [a-z]+\./i)

    const shortened = await shorten(request, { destinationUrl })
    expect(shortened.status()).toBe(503)

    const listed = await request.get(`${publicBaseUrl}/urls`)
    expect(listed.status()).toBe(503)

    const deleted = await deleteShortUrl(request, customAlias)
    expect(deleted.status()).toBe(503)

    const contract = await request.get(`${publicBaseUrl}/openapi.yaml`)
    expect(contract.status()).toBe(200)

    startAndWait('db')

    await expect
      .poll(async () => (await follow(request, customAlias)).status(), {
        timeout: 10_000,
      })
      .toBe(302)
  })
})
