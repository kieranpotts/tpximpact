/**
 * The web client's settings. They're read from the environment once, at
 * startup, and nothing else reads the environment.
 *
 * See: docs > design > concepts > configuration.
 */
export interface Config {
  /** Where the web client calls the API service, with no trailing slash. */
  readonly apiBaseUrl: string
}

export class ConfigError extends Error {
  constructor (readonly problems: string[]) {
    super(`Invalid configuration. ${problems.join(' ')}`)
    this.name = 'ConfigError'
  }
}

/**
 * Reads and validates every setting. Throws a `ConfigError`, listing every
 * missing or invalid setting, not just the first.
 */
export function loadConfig (env: NodeJS.ProcessEnv = process.env): Config {
  const problems: string[] = []

  let apiBaseUrl: string | undefined
  const rawApiBaseUrl = env.API_BASE_URL
  if (rawApiBaseUrl === undefined || rawApiBaseUrl.trim() === '') {
    problems.push('API_BASE_URL is not set.')
  } else if (isBaseUrl(rawApiBaseUrl)) {
    apiBaseUrl = rawApiBaseUrl.replace(/\/$/, '')
  } else {
    problems.push(
      'API_BASE_URL must be an http or https URL with a host, and no path, ' +
        'query, fragment, or user-info. For example, http://api:8080.'
    )
  }

  if (problems.length > 0 || apiBaseUrl === undefined) {
    throw new ConfigError(problems)
  }
  return Object.freeze({ apiBaseUrl })
}

/** The API service's paths are appended to the base URL, so it can't have one of its own. */
function isBaseUrl (value: string): boolean {
  if (!URL.canParse(value)) {
    return false
  }
  const url = new URL(value)
  return (
    (url.protocol === 'http:' || url.protocol === 'https:') &&
    url.hostname !== '' &&
    url.username === '' &&
    url.password === '' &&
    url.pathname === '/' &&
    !value.includes('?') &&
    !value.includes('#')
  )
}
