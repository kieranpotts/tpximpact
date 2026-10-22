import { expect, test, type Page } from '@playwright/test'
import { deleteShortUrl, follow, shorten, uniqueAlias } from './api.js'
import { publicBaseUrl } from './urls.js'

/** The table row for an alias. Other tests add rows of their own, in parallel. */
function rowFor (page: Page, alias: string) {
  return page
    .getByRole('table', { name: 'All short URLs' })
    .getByRole('row')
    .filter({ has: page.getByRole('rowheader', { name: alias, exact: true }) })
}

// Requirement: B10
test.describe('List and delete short URLs from the web client', () => {
  test('A link editor sees every short URL, with its alias and destination URL', async ({
    page,
    request,
  }) => {
    const customAlias = uniqueAlias('table')
    await shorten(request, { destinationUrl: 'https://listed.example/page', customAlias })

    await page.goto('/')

    const row = rowFor(page, customAlias)
    await expect(row).toHaveCount(1)
    await expect(row.getByRole('cell').nth(1)).toHaveText('https://listed.example/page')
    await expect(
      row.getByRole('link', { name: `${publicBaseUrl}/${customAlias}` })
    ).toHaveAttribute('href', `${publicBaseUrl}/${customAlias}`)
  })

  test('A short URL created in the form appears in the table', async ({ page }) => {
    const customAlias = uniqueAlias('form-table')

    await page.goto('/')
    await page.getByLabel('Destination URL').fill('https://from-form.example/')
    await page.getByLabel('Custom alias (optional)').fill(customAlias)
    await page.getByRole('button', { name: 'Shorten URL' }).click()

    await expect(page.getByRole('alert')).toContainText('Your short URL is ready')
    await expect(rowFor(page, customAlias)).toHaveCount(1)
  })

  test('A link editor deletes a short URL, which leaves the table and stops redirecting', async ({
    page,
    request,
  }) => {
    const customAlias = uniqueAlias('web-delete')
    await shorten(request, { destinationUrl: 'https://deleted.example/', customAlias })

    await page.goto('/')
    await rowFor(page, customAlias)
      .getByRole('button', { name: `Delete ${customAlias}` })
      .click()

    await expect(page.getByRole('alert')).toContainText(
      `The short URL ${customAlias} was deleted`
    )
    await expect(rowFor(page, customAlias)).toHaveCount(0)
    expect((await follow(request, customAlias)).status()).toBe(404)
  })

  test('Deleting a short URL that someone else already deleted still succeeds', async ({
    page,
    request,
  }) => {
    const customAlias = uniqueAlias('gone')
    await shorten(request, { destinationUrl: 'https://gone.example/', customAlias })

    await page.goto('/')
    await deleteShortUrl(request, customAlias)
    await rowFor(page, customAlias)
      .getByRole('button', { name: `Delete ${customAlias}` })
      .click()

    await expect(page.getByRole('alert')).toContainText(
      `The short URL ${customAlias} was deleted`
    )
  })
})
