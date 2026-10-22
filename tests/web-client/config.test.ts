import { describe, expect, it } from 'vitest'
import { ConfigError, loadConfig } from '../../src/web-client/src/config.js'

describe('loadConfig', () => {
  it('reads the API base URL, without a trailing slash', () => {
    expect(loadConfig({ API_BASE_URL: 'http://api:8080/' })).toEqual({
      apiBaseUrl: 'http://api:8080',
    })
  })

  it.each([undefined, '', '  '])('fails when API_BASE_URL is %j', (value) => {
    expect(() => loadConfig({ API_BASE_URL: value })).toThrow(
      new ConfigError(['API_BASE_URL is not set.'])
    )
  })

  it.each([
    'api:8080',
    'ftp://api:8080',
    'http://api:8080/v1',
    'http://api:8080/?a=b',
    'http://api:8080/#top',
    'http://user:pass@api:8080',
    'not a url',
  ])('fails when API_BASE_URL is %j', (value) => {
    expect(() => loadConfig({ API_BASE_URL: value })).toThrow(
      /API_BASE_URL must be an http or https URL/
    )
  })
})
