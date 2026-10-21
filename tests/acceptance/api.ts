import { randomBytes } from 'node:crypto'
import { expect, type APIRequestContext } from '@playwright/test'
import { publicBaseUrl } from './urls.js'

/** Calls `POST /shorten` on the API service, with a JSON body. */
export async function shorten (
  request: APIRequestContext,
  body: Record<string, unknown>
) {
  return request.post(`${publicBaseUrl}/shorten`, { data: body })
}

/** Calls `GET /{alias}` on the API service, without following the redirect. */
export async function follow (request: APIRequestContext, alias: string) {
  return request.get(`${publicBaseUrl}/${alias}`, { maxRedirects: 0 })
}

/** One item of the list that `GET /urls` returns. */
export interface ListedShortUrl {
  alias: string
  destinationUrl: string
  shortUrl: string
}

/** Calls `GET /urls` on the API service, and returns the list. */
export async function listShortUrls (request: APIRequestContext) {
  const response = await request.get(`${publicBaseUrl}/urls`)
  expect(response.status()).toBe(200)
  return (await response.json()) as ListedShortUrl[]
}

/** Calls `DELETE /{alias}` on the API service. */
export async function deleteShortUrl (
  request: APIRequestContext,
  alias: string
) {
  return request.delete(`${publicBaseUrl}/${alias}`)
}

/**
 * An alias that no other test uses, so that tests don't depend on each other,
 * or on what's already stored.
 */
export function uniqueAlias (prefix: string) {
  return `${prefix}-${randomBytes(6).toString('hex')}`
}

/** A destination URL that's valid, and on a host that's not the shortener's. */
export const destinationUrl = 'https://example.com/very/long/url'
