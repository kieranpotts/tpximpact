import { expect, test } from '@playwright/test'
import { destinationUrl, follow, uniqueAlias } from './api.js'
import { publicBaseUrl } from './urls.js'

// Requirement: B9
test.describe('Shorten a URL from the web client', () => {
  test('A link editor shortens a URL with a custom alias, and sees the short URL', async ({
    page,
    request,
  }) => {
    const customAlias = uniqueAlias('web')

    await page.goto('/')
    await page.getByLabel('Destination URL').fill(destinationUrl)
    await page.getByLabel('Custom alias (optional)').fill(customAlias)
    await page.getByRole('button', { name: 'Shorten URL' }).click()

    const banner = page.getByRole('alert')
    await expect(banner).toContainText('Your short URL is ready')
    await expect(
      banner.getByRole('link', { name: `${publicBaseUrl}/${customAlias}` })
    ).toHaveAttribute('href', `${publicBaseUrl}/${customAlias}`)

    const redirect = await follow(request, customAlias)
    expect(redirect.status()).toBe(302)
    expect(redirect.headers().location).toBe(destinationUrl)
  })

  test('A link editor who leaves the alias empty gets a random alias', async ({
    page,
  }) => {
    await page.goto('/')
    await page.getByLabel('Destination URL').fill(destinationUrl)
    await page.getByRole('button', { name: 'Shorten URL' }).click()

    await expect(
      page.getByRole('alert').getByRole('link', {
        name: new RegExp(`^${publicBaseUrl}/[A-Za-z0-9]{7}$`),
      })
    ).toBeVisible()
  })

  test('The form starts empty again after a URL is shortened', async ({
    page,
  }) => {
    await page.goto('/')
    await page.getByLabel('Destination URL').fill(destinationUrl)
    await page.getByRole('button', { name: 'Shorten URL' }).click()

    await expect(page.getByRole('alert')).toBeVisible()
    await expect(page.getByLabel('Destination URL')).toHaveValue('')
    await expect(page.getByLabel('Custom alias (optional)')).toHaveValue('')
  })
})
