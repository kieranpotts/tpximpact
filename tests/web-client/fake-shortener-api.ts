import type {
  ApiResult,
  ListedShortUrl,
  Shortened,
  ShortenerApi,
  ShortenRequest,
} from '../../src/web-client/src/api/shortener-api.js'

/**
 * Stands in for the API service in route tests. Each call returns the result
 * a test sets up, and records what it was sent.
 */
export class FakeShortenerApi implements ShortenerApi {
  shortenResult: ApiResult<Shortened> = { kind: 'unavailable' }
  listResult: ApiResult<ListedShortUrl[]> = { kind: 'ok', value: [] }
  readonly shortenRequests: ShortenRequest[] = []

  shorten (request: ShortenRequest): Promise<ApiResult<Shortened>> {
    this.shortenRequests.push(request)
    return Promise.resolve(this.shortenResult)
  }

  list (): Promise<ApiResult<ListedShortUrl[]>> {
    return Promise.resolve(this.listResult)
  }
}
