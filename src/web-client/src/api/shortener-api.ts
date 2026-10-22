/**
 * The web client's view of the API service. The types mirror the API
 * contract, by hand.
 *
 * See: docs > decisions > 0007.
 */
export interface ShortenerApi {
  /** Calls `POST /shorten`. */
  shorten(request: ShortenRequest): Promise<ApiResult<Shortened>>

  /** Calls `GET /urls`. */
  list(): Promise<ApiResult<ListedShortUrl[]>>

  /**
   * Calls `DELETE /{alias}`. An alias that doesn't exist counts as deleted,
   * so the result is `ok` for it too.
   */
  remove(alias: string): Promise<ApiResult<null>>
}

export interface ShortenRequest {
  destinationUrl: string
  /** Absent for a random alias. */
  customAlias?: string
}

export interface Shortened {
  shortUrl: string
}

export interface ListedShortUrl {
  alias: string
  destinationUrl: string
  shortUrl: string
}

/** An RFC 9457 problem details body, as the API service writes them. */
export interface Problem {
  type: string
  title: string
  status: number
  detail?: string
  /** Only in a `validation-failed` problem. */
  errors?: FieldError[]
}

export interface FieldError {
  field: string
  code: string
  /** Written for link editors, so it's shown verbatim. */
  message: string
}

/**
 * Every expected outcome of a call. The API client never throws for one of
 * these.
 *
 * See: docs > design > concepts > error-handling.
 */
export type ApiResult<T> =
  | { kind: 'ok', value: T }
  /** A `4xx`. `problem` is absent when the body isn't a problem. */
  | { kind: 'rejected', problem?: Problem }
  /** A connection failure, a timeout, a `5xx`, or a body that can't be understood. */
  | { kind: 'unavailable' }

export const problemTypes = {
  aliasTaken: 'urn:problem:url-shortener:alias-taken',
  validationFailed: 'urn:problem:url-shortener:validation-failed',
} as const
