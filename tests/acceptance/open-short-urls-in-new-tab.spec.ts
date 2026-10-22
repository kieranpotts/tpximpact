import { expect, test, type Locator, type Page } from '@playwright/test'
import { destinationUrl, shorten, uniqueAlias } from './api.js'
import { publicBaseUrl } from './urls.js'

/**
 * Serves a short URL from inside the browser, so the new tab doesn't leave
 * for the destination, which the test can't reach. Redirects are verified
 * elsewhere (B5).
 */
async function serveInBrowser (page: Page, shortUrl: string) {
  await page.context().route(shortUrl, (route) =>
    route.fulfill({ contentType: 'text/html', body: '<title>Short URL</title>' })
  )
}

/** Clicks a short URL's link, and returns the tab it opens. */
async function clickIntoNewTab (page: Page, within: Locator, shortUrl: string) {
  const newTab = page.waitForEvent('popup')
  await within.getByRole('link', { name: `${shortUrl} (opens in new tab)` }).click()
  return newTab
}

// Requirement: B16
test.describe('Open short URLs in a new tab', () => {
  test('The short URL just created opens in a new tab, and the web client stays put', async ({
    page,
  }) => {
    const customAlias = uniqueAlias('tab-banner')
    const shortUrl = `${publicBaseUrl}/${customAlias}`
    await serveInBrowser(page, shortUrl)

    await page.goto('/')
    await page.getByLabel('Destination URL').fill(destinationUrl)
    await page.getByLabel('Custom alias (optional)').fill(customAlias)
    await page.getByRole('button', { name: 'Shorten URL' }).click()
    const banner = page.getByRole('alert')
    await expect(banner).toContainText('Your short URL is ready')
    const webClientPage = page.url()

    const newTab = await clickIntoNewTab(page, banner, shortUrl)

    await expect(newTab).toHaveURL(shortUrl)
    expect(page.url()).toBe(webClientPage)
  })

  test('A short URL in the table opens in a new tab, and the web client stays put', async ({
    page,
    request,
  }) => {
    const customAlias = uniqueAlias('tab-table')
    const shortUrl = `${publicBaseUrl}/${customAlias}`
    await shorten(request, { destinationUrl, customAlias })
    await serveInBrowser(page, shortUrl)

    await page.goto('/')
    const webClientPage = page.url()

    const newTab = await clickIntoNewTab(
      page,
      page.getByRole('table', { name: 'All short URLs' }),
      shortUrl
    )

    await expect(newTab).toHaveURL(shortUrl)
    expect(page.url()).toBe(webClientPage)
  })
})
