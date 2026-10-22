import express, { type Response, type Router } from 'express'
import type { ListedShortUrl, ShortenerApi, ShortenRequest } from '../api/shortener-api.js'
import {
  type FormErrors,
  presentDeleteRejection,
  presentRejection,
  presentUnavailable,
} from '../presentation/error-presenter.js'

/** What the link editor entered in the shorten form, kept to show it again. */
interface ShortenForm {
  destinationUrl: string
  customAlias: string
}

const emptyForm: ShortenForm = { destinationUrl: '', customAlias: '' }

/** What the home page shows, besides the table of short URLs. */
interface HomePage {
  form: ShortenForm
  errors?: FormErrors
  created?: ListedShortUrl | undefined
  /** The alias just deleted. */
  deleted?: string | undefined
}

/**
 * The home page, with the shorten form and the table of short URLs, and the
 * handlers for the shorten form and each row's delete button. A shortened or
 * deleted URL is shown by redirecting back to the home page, with its alias
 * in the query (Post/Redirect/Get).
 *
 * See: docs > design > scenarios.
 */
export function linkRoutes (api: ShortenerApi): Router {
  const router = express.Router()

  router.get('/', async (req, res) => {
    const { created, deleted } = req.query
    const shortUrls = await listOrUndefined(api)
    renderHome(res, 200, shortUrls, {
      form: emptyForm,
      created: typeof created === 'string' ? findCreated(shortUrls, created) : undefined,
      deleted: typeof deleted === 'string' && !isListed(shortUrls, deleted) ? deleted : undefined,
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
          renderHome(res, 400, await listOrUndefined(api), {
            form,
            errors: presentRejection(result.problem),
          })
          return
        case 'unavailable':
          // The list would fail too, after making the link editor wait.
          renderHome(res, 503, undefined, { form, errors: presentUnavailable() })
      }
    }
  )

  router.post('/links/:alias/delete', async (req, res) => {
    const { alias } = req.params
    const result = await api.remove(alias)

    switch (result.kind) {
      case 'ok':
        res.redirect(303, `/?deleted=${encodeURIComponent(alias)}`)
        return
      case 'rejected':
        renderHome(res, 400, await listOrUndefined(api), {
          form: emptyForm,
          errors: presentDeleteRejection(),
        })
        return
      case 'unavailable':
        renderHome(res, 503, undefined, { form: emptyForm, errors: presentUnavailable() })
    }
  })

  return router
}

/**
 * Renders the home page. `shortUrls` is `undefined` when they couldn't be
 * loaded, and the page says so in place of the table.
 */
function renderHome (
  res: Response,
  status: number,
  shortUrls: ListedShortUrl[] | undefined,
  page: HomePage
): void {
  res.status(status).render('index.njk', { ...page, shortUrls })
}

/** Every short URL, or `undefined` when they can't be loaded. */
async function listOrUndefined (api: ShortenerApi): Promise<ListedShortUrl[] | undefined> {
  const listed = await api.list()
  return listed.kind === 'ok' ? listed.value : undefined
}

/**
 * Looks the new short URL up, rather than taking it from the query, so that a
 * crafted link can't make the page vouch for a URL the system didn't make.
 */
function findCreated (shortUrls: ListedShortUrl[] | undefined, alias: string): ListedShortUrl | undefined {
  return shortUrls?.find((shortUrl) => shortUrl.alias === alias)
}

/**
 * Whether the alias is in the list. A crafted link can't make the page say
 * that a short URL which still exists was deleted.
 */
function isListed (shortUrls: ListedShortUrl[] | undefined, alias: string): boolean {
  return shortUrls?.some((shortUrl) => shortUrl.alias === alias) ?? false
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
