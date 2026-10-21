import { randomBytes } from 'node:crypto'
import type { APIRequestContext } from '@playwright/test'
import { publicBaseUrl } from './urls.js'

/** Calls `POST /shorten` on the API service, with a JSON body. */
export async function shorten (
  request: APIRequestContext,
  body: Record<string, unknown>
) {
  return request.post(`${publicBaseUrl}/shorten`, { data: body })
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
