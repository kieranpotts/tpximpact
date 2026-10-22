import { expect, test } from '@playwright/test'
import { destinationUrl, shorten, uniqueAlias } from './api.js'

// Requirement: B11
//
// The API service being unreachable is covered by the web client's own tests.
// See: docs > decisions > 0017.
test.describe('Show API errors in the web client', () => {
  test('A link editor sees why the API service rejected the URL, and keeps what they entered', async ({
    page,
  }) => {
    const customAlias = uniqueAlias('rejected')

    await page.goto('/')
    await page.getByLabel('Destination URL').fill('ftp://example.com/file')
    await page.getByLabel('Custom alias (optional)').fill(customAlias)
    await page.getByRole('button', { name: 'Shorten URL' }).click()

    await expect(page).toHaveTitle(/^Error: /)
    const summary = page.getByRole('alert')
    await expect(summary).toContainText('There is a problem')
    await expect(
      summary.getByRole('link', { name: 'Enter a URL starting with http:// or https://' })
    ).toBeVisible()
    await expect(page.getByLabel('Destination URL')).toHaveValue('ftp://example.com/file')
    await expect(page.getByLabel('Custom alias (optional)')).toHaveValue(customAlias)
  })

  test('A link editor sees that their alias is taken, and keeps what they entered', async ({
    page,
    request,
  }) => {
    const customAlias = uniqueAlias('taken')
    await shorten(request, { destinationUrl, customAlias })

    await page.goto('/')
    await page.getByLabel('Destination URL').fill('https://second.example/')
    await page.getByLabel('Custom alias (optional)').fill(customAlias)
    await page.getByRole('button', { name: 'Shorten URL' }).click()

    await expect(
      page.getByRole('alert').getByRole('link', { name: 'That alias is already taken. Enter a different alias.' })
    ).toBeVisible()
    await expect(page.getByLabel('Destination URL')).toHaveValue('https://second.example/')
    await expect(page.getByLabel('Custom alias (optional)')).toHaveValue(customAlias)
  })
})
