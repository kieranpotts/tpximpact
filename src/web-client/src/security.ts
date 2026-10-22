import { randomBytes } from 'node:crypto'
import type { IncomingMessage, ServerResponse } from 'node:http'
import type { NextFunction, Request, RequestHandler, Response } from 'express'
import helmet from 'helmet'

declare module 'express-serve-static-core' {
  interface Locals {
    /** Allows the page's inline scripts, and no others. Fresh for every response. */
    cspNonce: string
  }
}

/**
 * Gives each response a fresh CSP nonce, which the views put on their inline
 * scripts.
 */
export function cspNonce (_req: Request, res: Response, next: NextFunction): void {
  res.locals.cspNonce = randomBytes(16).toString('base64')
  next()
}

/**
 * Sets the security headers. Scripts and styles may come only from the web
 * client itself, plus inline scripts that carry the response's nonce. Must
 * follow `cspNonce`.
 *
 * See: docs > design > concepts > security.
 */
export function securityHeaders (): RequestHandler {
  return helmet({
    contentSecurityPolicy: {
      directives: {
        scriptSrc: [
          "'self'",
          (_req: IncomingMessage, res: ServerResponse) =>
            `'nonce-${(res as Response).locals.cspNonce}'`,
        ],
        styleSrc: ["'self'"],
        fontSrc: ["'self'"],
        frameAncestors: ["'none'"],
        // The MVP serves plain HTTP. See: docs > design > concepts > security.
        upgradeInsecureRequests: null,
      },
    },
    // Not `no-referrer`, helmet's default, under which browsers send
    // `Origin: null` with every form post, so `rejectCrossOriginPosts` would
    // refuse the web client's own forms.
    referrerPolicy: { policy: 'same-origin' },
  })
}

/**
 * Refuses form posts from other sites, so that no other site can make a link
 * editor's browser shorten or delete URLs. Browsers send `Origin` with every
 * cross-origin `POST`. Requests without one don't come from another site's
 * page, so they're let through.
 */
export function rejectCrossOriginPosts (req: Request, res: Response, next: NextFunction): void {
  const origin = req.get('Origin')
  if (req.method !== 'POST' || origin === undefined || isSameHost(origin, req.get('Host'))) {
    next()
    return
  }
  res.status(403).render('error.njk', {
    title: 'Forbidden',
    message: 'This form was sent from another website, so it was not accepted.',
  })
}

function isSameHost (origin: string, host: string | undefined): boolean {
  return URL.canParse(origin) && new URL(origin).host === host
}
