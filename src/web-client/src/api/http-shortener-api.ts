import type {
  ApiResult,
  ListedShortUrl,
  Problem,
  Shortened,
  ShortenerApi,
  ShortenRequest,
} from './shortener-api.js'

/**
 * Calls the API service over HTTP. Each call gives up after `timeoutMs`, and
 * counts as `unavailable`.
 */
export class HttpShortenerApi implements ShortenerApi {
  constructor (
    private readonly baseUrl: string,
    private readonly timeoutMs: number
  ) {}

  async shorten (request: ShortenRequest): Promise<ApiResult<Shortened>> {
    return this.call('/shorten', 'POST', request, (body) =>
      isShortened(body) ? body : undefined
    )
  }

  async list (): Promise<ApiResult<ListedShortUrl[]>> {
    return this.call('/urls', 'GET', undefined, (body) =>
      Array.isArray(body) && body.every(isListedShortUrl) ? body : undefined
    )
  }

  async remove (alias: string): Promise<ApiResult<null>> {
    // The URL parser would resolve these as dot segments, even encoded, and
    // call a different path. Neither can be an alias, so the API service
    // would reject them anyway.
    if (alias === '.' || alias === '..') {
      return { kind: 'rejected' }
    }
    // A `204` has no body to parse.
    return this.call(`/${encodeURIComponent(alias)}`, 'DELETE', undefined, () => null)
  }

  /**
   * Makes one call, with an optional JSON body, and sorts its outcome into a
   * result. `parse` returns `undefined` for a `2xx` body it can't understand.
   */
  private async call<T>(
    path: string,
    method: 'GET' | 'POST' | 'DELETE',
    json: object | undefined,
    parse: (body: unknown) => T | undefined
  ): Promise<ApiResult<T>> {
    let response: Response
    let text: string
    try {
      response = await fetch(`${this.baseUrl}${path}`, {
        method,
        headers: json === undefined
          ? { Accept: 'application/json' }
          : { Accept: 'application/json', 'Content-Type': 'application/json' },
        body: json === undefined ? null : JSON.stringify(json),
        redirect: 'manual',
        signal: AbortSignal.timeout(this.timeoutMs),
      })
      text = await response.text()
    } catch {
      // A connection failure, or the timeout.
      return { kind: 'unavailable' }
    }

    const body = parseJson(text)
    if (response.status >= 200 && response.status < 300) {
      const value = parse(body)
      return value === undefined ? { kind: 'unavailable' } : { kind: 'ok', value }
    }
    if (response.status >= 400 && response.status < 500) {
      return isProblem(body)
        ? { kind: 'rejected', problem: body }
        : { kind: 'rejected' }
    }
    return { kind: 'unavailable' }
  }
}

function parseJson (text: string): unknown {
  try {
    return JSON.parse(text)
  } catch {
    return undefined
  }
}

function isObject (value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function isShortened (value: unknown): value is Shortened {
  return isObject(value) && typeof value.shortUrl === 'string'
}

function isListedShortUrl (value: unknown): value is ListedShortUrl {
  return (
    isObject(value) &&
    typeof value.alias === 'string' &&
    typeof value.destinationUrl === 'string' &&
    typeof value.shortUrl === 'string'
  )
}

function isProblem (value: unknown): value is Problem {
  return (
    isObject(value) &&
    typeof value.type === 'string' &&
    typeof value.title === 'string' &&
    typeof value.status === 'number' &&
    (value.detail === undefined || typeof value.detail === 'string') &&
    (value.errors === undefined ||
      (Array.isArray(value.errors) &&
        value.errors.every(
          (error) =>
            isObject(error) &&
            typeof error.field === 'string' &&
            typeof error.code === 'string' &&
            typeof error.message === 'string'
        )))
  )
}
