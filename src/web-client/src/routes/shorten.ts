import express, { type Router } from 'express'
import type { ListedShortUrl, ShortenerApi, ShortenRequest } from '../api/shortener-api.js'
import { presentRejection, presentUnavailable } from '../presentation/error-presenter.js'

/** What the link editor entered in the shorten form, kept to show it again. */
interface ShortenForm {
  destinationUrl: string
  customAlias: string
}

const emptyForm: ShortenForm = { destinationUrl: '', customAlias: '' }

/**
 * The home page, with the shorten form, and the form's handler. A shortened
 * URL is shown by redirecting back to the home page, with its alias in the
 * query (Post/Redirect/Get).
 *
 * See: docs > design > scenarios.
 */
export function shortenRoutes (api: ShortenerApi): Router {
  const router = express.Router()

  router.get('/', async (req, res) => {
    const { created } = req.query
    res.render('index.njk', {
      form: emptyForm,
      created: typeof created === 'string' ? await findCreated(api, created) : undefined,
    })
  })

  router.post(
    '/links',
    express.urlencoded({ extended: false, limit: '32kb' }),
    async (req, res) => {
      const form = readForm(req.body)
      const result = await api.shorten(toRequest(form))

      switch (result.kind) {
        case 'ok': {
          const alias = aliasOf(result.value.shortUrl)
          res.redirect(303, alias === undefined ? '/' : `/?created=${encodeURIComponent(alias)}`)
          return
        }
        case 'rejected':
          res.status(400).render('index.njk', { form, errors: presentRejection(result.problem) })
          return
        case 'unavailable':
          res.status(503).render('index.njk', { form, errors: presentUnavailable() })
      }
    }
  )

  return router
}

/**
 * Looks the new short URL up, rather than taking it from the query, so that a
 * crafted link can't make the page vouch for a URL the system didn't make.
 */
async function findCreated (api: ShortenerApi, alias: string): Promise<ListedShortUrl | undefined> {
  const result = await api.list()
  return result.kind === 'ok'
    ? result.value.find((shortUrl) => shortUrl.alias === alias)
    : undefined
}

function readForm (body: unknown): ShortenForm {
  const field = (name: keyof ShortenForm): string => {
    const value: unknown = typeof body === 'object' && body !== null
      ? (body as Record<string, unknown>)[name]
      : undefined
    return typeof value === 'string' ? value : ''
  }
  return { destinationUrl: field('destinationUrl'), customAlias: field('customAlias') }
}

/**
 * An empty alias field asks for a random alias. Whitespace around the alias
 * can't be seen in the field, so it's dropped.
 */
function toRequest ({ destinationUrl, customAlias }: ShortenForm): ShortenRequest {
  const alias = customAlias.trim()
  return alias === '' ? { destinationUrl } : { destinationUrl, customAlias: alias }
}

/** A short URL is the public base URL followed by the alias. */
function aliasOf (shortUrl: string): string | undefined {
  if (!URL.canParse(shortUrl)) {
    return undefined
  }
  const alias = new URL(shortUrl).pathname.split('/').pop()
  return alias === '' ? undefined : alias
}
