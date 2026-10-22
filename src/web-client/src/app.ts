import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import express, { type ErrorRequestHandler, type Express } from 'express'
import nunjucks from 'nunjucks'
import type { ShortenerApi } from './api/shortener-api.js'
import { shortenRoutes } from './routes/shorten.js'
import { cspNonce, rejectCrossOriginPosts, securityHeaders } from './security.js'

// These paths hold from both `src/` and `dist/`.
const viewsDir = fileURLToPath(new URL('../views', import.meta.url))
const govukDir = fileURLToPath(new URL('../node_modules/govuk-frontend/dist', import.meta.url))

// GOV.UK Frontend's licence restricts its crown and its font, GDS Transport,
// to government services. So the web client serves none of its images or
// fonts, and drops the CSS rules that would load the font. Browsers fall back
// to Arial. See: docs > decisions > 0008.
const govukCss = readFileSync(`${govukDir}/govuk/govuk-frontend.min.css`, 'utf8')
  .replace(/@font-face\{[^}]*\}/g, '')

export interface AppDependencies {
  api: ShortenerApi
}

export function createApp ({ api }: AppDependencies): Express {
  const app = express()
  app.disable('x-powered-by')

  const views = new nunjucks.Environment(
    new nunjucks.FileSystemLoader([viewsDir, govukDir]),
    { autoescape: true }
  )
  views.express(app)
  app.set('view engine', 'njk')

  app.use(cspNonce, securityHeaders(), rejectCrossOriginPosts)

  app.get('/assets/govuk-frontend.min.css', (_req, res) => {
    res.type('css').send(govukCss)
  })
  app.get('/assets/govuk-frontend.min.js', (_req, res) => {
    res.sendFile(`${govukDir}/govuk/govuk-frontend.min.js`)
  })

  app.use(shortenRoutes(api))

  app.use((_req, res) => {
    res.status(404).render('error.njk', {
      title: 'Page not found',
      message: 'If you typed the web address, check it is correct.',
    })
  })
  app.use(errorHandler)

  return app
}

/** Logs an unexpected fault, and shows a page that gives nothing away. */
const errorHandler: ErrorRequestHandler = (error, _req, res, _next) => {
  console.error(error)
  res.status(500).render('error.njk', {
    title: 'Sorry, there is a problem with the service',
    message: 'Try again later.',
  })
}
