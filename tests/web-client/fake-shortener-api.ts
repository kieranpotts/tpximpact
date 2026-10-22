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
  removeResult: ApiResult<null> = { kind: 'ok', value: null }
  readonly shortenRequests: ShortenRequest[] = []
  readonly removedAliases: string[] = []

  shorten (request: ShortenRequest): Promise<ApiResult<Shortened>> {
    this.shortenRequests.push(request)
    return Promise.resolve(this.shortenResult)
  }

  list (): Promise<ApiResult<ListedShortUrl[]>> {
    return Promise.resolve(this.listResult)
  }

  remove (alias: string): Promise<ApiResult<null>> {
    this.removedAliases.push(alias)
    return Promise.resolve(this.removeResult)
  }
}
