import { expect, test } from '@playwright/test'
import { publicBaseUrl } from './urls.js'

// Requirement: B14
test.describe('Publish the API contract', () => {
  test('The API service serves its OpenAPI contract, as valid YAML', async ({
    request,
  }) => {
    const response = await request.get(`${publicBaseUrl}/openapi.yaml`)

    expect(response.status()).toBe(200)
    expect(response.headers()['content-type']).toBe('application/yaml')
    expect(await response.text()).toMatch(/^openapi: 3\.0\.\d+$/m)
  })
})
