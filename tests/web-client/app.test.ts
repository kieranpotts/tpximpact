import * as cheerio from 'cheerio'
import request from 'supertest'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { ApiResult, ListedShortUrl } from '../../src/web-client/src/api/shortener-api.js'
import { createApp } from '../../src/web-client/src/app.js'
import { FakeShortenerApi } from './fake-shortener-api.js'

let api: FakeShortenerApi

beforeEach(() => {
  api = new FakeShortenerApi()
})

function app () {
  return createApp({ api })
}

/** Submits the shorten form, as a browser on the web client's own page would. */
function submit (form: Record<string, string>) {
  return request(app())
    .post('/links')
    .set('Origin', 'http://127.0.0.1')
    .set('Host', '127.0.0.1')
    .type('form')
    .send(form)
}

/** Presses a row's delete button, as a browser on the web client's own page would. */
function remove (alias: string) {
  return request(app())
    .post(`/links/${encodeURIComponent(alias)}/delete`)
    .set('Origin', 'http://127.0.0.1')
    .set('Host', '127.0.0.1')
}

const listed: ListedShortUrl[] = [
  { alias: 'newest', destinationUrl: 'https://newest.example/', shortUrl: 'http://localhost:8080/newest' },
  { alias: 'oldest', destinationUrl: 'https://oldest.example/a?b=c', shortUrl: 'http://localhost:8080/oldest' },
]

/** Each row of the table of short URLs, as the text of its cells. */
function tableRows ($: cheerio.CheerioAPI): string[][] {
  return $('table tbody tr')
    .map((_, row) => [$(row).children().map((_, cell) => $(cell).text().trim()).get()])
    .get<string[]>()
}

function errorSummary ($: cheerio.CheerioAPI) {
  return $('.govuk-error-summary__list li')
    .map((_, element) => $(element).text().trim())
    .get()
}

describe('the home page', () => {
  it('shows the empty shorten form', async () => {
    const response = await request(app()).get('/')

    expect(response.status).toBe(200)
    expect(response.type).toBe('text/html')
    const $ = cheerio.load(response.text)
    expect($('title').text()).toBe('Shorten a URL - URL shortener')
    expect($('h1').text()).toBe('Shorten a URL')
    expect($('form').attr('method')).toBe('post')
    expect($('form').attr('action')).toBe('/links')
    expect($('label[for=destinationUrl]').text().trim()).toBe('Destination URL')
    expect($('#destinationUrl').val()).toBe('')
    expect($('label[for=customAlias]').text().trim()).toBe('Custom alias (optional)')
    expect($('#customAlias').val()).toBe('')
    expect($('.govuk-error-summary')).toHaveLength(0)
    expect($('.govuk-notification-banner')).toHaveLength(0)
  })

  it('leaves out the crown and the GOV.UK font, which only government services may use', async () => {
    const response = await request(app()).get('/')

    expect(response.text).not.toMatch(/crown|crest|favicon|\/assets\/(fonts|images)\//i)
  })

  it('shows the short URL just created, looked up from the API service', async () => {
    api.listResult = {
      kind: 'ok',
      value: [
        { alias: 'other', destinationUrl: 'https://other.example/', shortUrl: 'http://localhost:8080/other' },
        { alias: 'my-alias', destinationUrl: 'https://example.com/long', shortUrl: 'http://localhost:8080/my-alias' },
      ],
    }

    const response = await request(app()).get('/?created=my-alias')

    expect(response.status).toBe(200)
    const banner = cheerio.load(response.text)('.govuk-notification-banner--success')
    expect(banner.find('.govuk-notification-banner__heading').text()).toBe('Your short URL is ready')
    expect(banner.find('a').attr('href')).toBe('http://localhost:8080/my-alias')
    expect(banner.find('a').text()).toBe('http://localhost:8080/my-alias')
    expect(banner.text()).toContain('It redirects to https://example.com/long')
  })

  it.each<[string, ApiResult<ListedShortUrl[]>]>([
    ['an alias that does not exist', { kind: 'ok', value: [] }],
    ['an alias it cannot look up', { kind: 'unavailable' }],
  ])('shows no short URL for %s', async (_, listResult) => {
    api.listResult = listResult

    const response = await request(app()).get('/?created=my-alias')

    expect(response.status).toBe(200)
    expect(cheerio.load(response.text)('.govuk-notification-banner')).toHaveLength(0)
  })

  it('escapes what it shows', async () => {
    api.listResult = {
      kind: 'ok',
      value: [{ alias: 'x', destinationUrl: 'https://example.com/<script>', shortUrl: 'http://localhost:8080/x' }],
    }

    const response = await request(app()).get('/?created=x')

    expect(response.text).toContain('https://example.com/&lt;script&gt;')
    expect(response.text).not.toContain('<script>')
  })
})

describe('the table of short URLs', () => {
  it('lists every short URL, in the order the API service gives', async () => {
    api.listResult = { kind: 'ok', value: listed }

    const response = await request(app()).get('/')

    expect(response.status).toBe(200)
    const $ = cheerio.load(response.text)
    expect($('table').attr('aria-labelledby')).toBe('short-urls')
    expect($('#short-urls').text()).toBe('All short URLs')
    expect($('table thead th').map((_, th) => $(th).text().trim()).get()).toEqual([
      'Alias',
      'Short URL',
      'Destination URL',
      'Actions',
    ])
    expect(tableRows($)).toEqual([
      ['newest', 'http://localhost:8080/newest', 'https://newest.example/', 'Delete newest'],
      ['oldest', 'http://localhost:8080/oldest', 'https://oldest.example/a?b=c', 'Delete oldest'],
    ])
    expect($('table tbody a').map((_, a) => $(a).attr('href')).get()).toEqual([
      'http://localhost:8080/newest',
      'http://localhost:8080/oldest',
    ])
  })

  it('gives each row a delete button that posts to that alias', async () => {
    api.listResult = { kind: 'ok', value: listed }

    const response = await request(app()).get('/')

    const $ = cheerio.load(response.text)
    const forms = $('table form')
    expect(forms.map((_, form) => $(form).attr('method')).get()).toEqual(['post', 'post'])
    expect(forms.map((_, form) => $(form).attr('action')).get()).toEqual([
      '/links/newest/delete',
      '/links/oldest/delete',
    ])
    expect(forms.find('button .govuk-visually-hidden').map((_, span) => $(span).text()).get()).toEqual([
      ' newest',
      ' oldest',
    ])
  })

  it('says when there are no short URLs', async () => {
    api.listResult = { kind: 'ok', value: [] }

    const response = await request(app()).get('/')

    const $ = cheerio.load(response.text)
    expect($('table')).toHaveLength(0)
    expect($('#short-urls').next().text()).toBe('There are no short URLs yet.')
  })

  it('says when the short URLs cannot be loaded, and still shows the form', async () => {
    api.listResult = { kind: 'unavailable' }

    const response = await request(app()).get('/')

    expect(response.status).toBe(200)
    const $ = cheerio.load(response.text)
    expect($('table')).toHaveLength(0)
    expect($('#short-urls').next().text()).toBe('The short URLs could not be loaded. Try again later.')
    expect($('#destinationUrl')).toHaveLength(1)
  })

  it('escapes what it shows', async () => {
    api.listResult = {
      kind: 'ok',
      value: [{ alias: 'x"y', destinationUrl: 'https://example.com/<script>', shortUrl: 'http://localhost:8080/x' }],
    }

    const response = await request(app()).get('/')

    expect(response.text).toContain('https://example.com/&lt;script&gt;')
    expect(response.text).not.toContain('<script>')
    expect(cheerio.load(response.text)('table form').attr('action')).toBe('/links/x%22y/delete')
  })
})

describe('the shorten form', () => {
  it('shortens with a custom alias, and redirects to the home page to show it', async () => {
    api.shortenResult = { kind: 'ok', value: { shortUrl: 'http://localhost:8080/my-alias' } }

    const response = await submit({ destinationUrl: 'https://example.com/', customAlias: ' my-alias ' })

    expect(api.shortenRequests).toEqual([{ destinationUrl: 'https://example.com/', customAlias: 'my-alias' }])
    expect(response.status).toBe(303)
    expect(response.headers.location).toBe('/?created=my-alias')
  })

  it('asks for a random alias when the alias field is empty', async () => {
    api.shortenResult = { kind: 'ok', value: { shortUrl: 'http://localhost:8080/aZ3k9Qx' } }

    const response = await submit({ destinationUrl: 'https://example.com/', customAlias: '  ' })

    expect(api.shortenRequests).toEqual([{ destinationUrl: 'https://example.com/' }])
    expect(response.headers.location).toBe('/?created=aZ3k9Qx')
  })

  it('sends the destination URL as entered, for the API service to judge', async () => {
    await submit({ destinationUrl: '  not a url ' })

    expect(api.shortenRequests).toEqual([{ destinationUrl: '  not a url ' }])
  })

  it('shows each invalid field, and keeps what was entered', async () => {
    api.shortenResult = {
      kind: 'rejected',
      problem: {
        type: 'urn:problem:url-shortener:validation-failed',
        title: 'The request is not valid',
        status: 400,
        errors: [
          { field: 'customAlias', code: 'alias-reserved', message: 'Enter an alias that is not reserved' },
          { field: 'destinationUrl', code: 'url-unsupported-scheme', message: 'Enter a URL starting with http:// or https://' },
          { field: 'destinationUrl', code: 'url-too-long', message: 'A second error on the same field' },
        ],
      },
    }

    const response = await submit({ destinationUrl: 'ftp://example.com/', customAlias: 'api' })

    expect(response.status).toBe(400)
    const $ = cheerio.load(response.text)
    expect($('title').text()).toBe('Error: Shorten a URL - URL shortener')
    expect(errorSummary($)).toEqual([
      'Enter a URL starting with http:// or https://',
      'Enter an alias that is not reserved',
    ])
    expect($('.govuk-error-summary__list a').map((_, a) => $(a).attr('href')).get()).toEqual([
      '#destinationUrl',
      '#customAlias',
    ])
    expect($('#destinationUrl-error').text()).toContain('Enter a URL starting with http:// or https://')
    expect($('#customAlias-error').text()).toContain('Enter an alias that is not reserved')
    expect($('#destinationUrl').val()).toBe('ftp://example.com/')
    expect($('#customAlias').val()).toBe('api')
  })

  it('still shows the table of short URLs after a rejection', async () => {
    api.shortenResult = { kind: 'rejected' }
    api.listResult = { kind: 'ok', value: listed }

    const response = await submit({ destinationUrl: 'https://example.com/' })

    expect(tableRows(cheerio.load(response.text))).toHaveLength(2)
  })

  it('shows a taken alias against the alias field', async () => {
    api.shortenResult = {
      kind: 'rejected',
      problem: { type: 'urn:problem:url-shortener:alias-taken', title: 'The alias is already taken', status: 400 },
    }

    const response = await submit({ destinationUrl: 'https://example.com/', customAlias: 'taken' })

    expect(response.status).toBe(400)
    const $ = cheerio.load(response.text)
    expect(errorSummary($)).toEqual(['That alias is already taken. Enter a different alias.'])
    expect($('#customAlias-error').text()).toContain('That alias is already taken')
    expect($('#destinationUrl').val()).toBe('https://example.com/')
    expect($('#customAlias').val()).toBe('taken')
  })

  it.each([
    ['no problem details', undefined],
    ['a problem it does not know', { type: 'urn:problem:url-shortener:payload-too-large', title: 'Too large', status: 413 }],
  ])('shows a general error for a rejection with %s', async (_, problem) => {
    api.shortenResult = problem === undefined ? { kind: 'rejected' } : { kind: 'rejected', problem }

    const response = await submit({ destinationUrl: 'https://example.com/', customAlias: 'mine' })

    expect(response.status).toBe(400)
    const $ = cheerio.load(response.text)
    expect(errorSummary($)).toEqual([
      'Your short URL could not be created. Check what you entered, and try again.',
    ])
    expect($('#destinationUrl').val()).toBe('https://example.com/')
    expect($('#customAlias').val()).toBe('mine')
  })

  it('says when the API service is unavailable, and keeps what was entered', async () => {
    api.shortenResult = { kind: 'unavailable' }

    const response = await submit({ destinationUrl: 'https://example.com/', customAlias: 'mine' })

    expect(response.status).toBe(503)
    const $ = cheerio.load(response.text)
    expect(errorSummary($)).toEqual(['The service is unavailable. Try again later.'])
    expect($('#destinationUrl').val()).toBe('https://example.com/')
    expect($('#customAlias').val()).toBe('mine')
    expect($('#short-urls').next().text()).toBe('The short URLs could not be loaded. Try again later.')
  })

  it('treats missing fields as empty', async () => {
    await request(app()).post('/links')

    expect(api.shortenRequests).toEqual([{ destinationUrl: '' }])
  })

  it('refuses a post from another website', async () => {
    const response = await request(app())
      .post('/links')
      .set('Origin', 'http://evil.example')
      .type('form')
      .send({ destinationUrl: 'https://example.com/' })

    expect(response.status).toBe(403)
    expect(api.shortenRequests).toEqual([])
  })
})

describe('the delete buttons', () => {
  it('delete the short URL, and redirect to the home page to say so', async () => {
    const response = await remove('my-alias')

    expect(api.removedAliases).toEqual(['my-alias'])
    expect(response.status).toBe(303)
    expect(response.headers.location).toBe('/?deleted=my-alias')
  })

  it('pass the alias on as it was in the path', async () => {
    const response = await remove('a b/c')

    expect(api.removedAliases).toEqual(['a b/c'])
    expect(response.headers.location).toBe('/?deleted=a%20b%2Fc')
  })

  it('show the rejection, and the table, when the API service rejects the delete', async () => {
    api.removeResult = { kind: 'rejected' }
    api.listResult = { kind: 'ok', value: listed }

    const response = await remove('ab')

    expect(response.status).toBe(400)
    const $ = cheerio.load(response.text)
    expect($('title').text()).toBe('Error: Shorten a URL - URL shortener')
    expect(errorSummary($)).toEqual([
      'The short URL could not be deleted. Refresh the page, and try again.',
    ])
    expect(tableRows($)).toHaveLength(2)
  })

  it('say when the API service is unavailable', async () => {
    api.removeResult = { kind: 'unavailable' }
    api.listResult = { kind: 'ok', value: listed }

    const response = await remove('my-alias')

    expect(response.status).toBe(503)
    const $ = cheerio.load(response.text)
    expect(errorSummary($)).toEqual(['The service is unavailable. Try again later.'])
    expect($('#short-urls').next().text()).toBe('The short URLs could not be loaded. Try again later.')
  })

  it('refuse a post from another website', async () => {
    const response = await request(app())
      .post('/links/my-alias/delete')
      .set('Origin', 'http://evil.example')

    expect(response.status).toBe(403)
    expect(api.removedAliases).toEqual([])
  })
})

describe('the deleted banner', () => {
  it('says which short URL was deleted', async () => {
    api.listResult = { kind: 'ok', value: listed }

    const response = await request(app()).get('/?deleted=my-alias')

    expect(response.status).toBe(200)
    const banner = cheerio.load(response.text)('.govuk-notification-banner--success')
    expect(banner.find('.govuk-notification-banner__heading').text().trim())
      .toBe('The short URL my-alias was deleted')
  })

  it('is not shown for a short URL that still exists', async () => {
    api.listResult = { kind: 'ok', value: listed }

    const response = await request(app()).get('/?deleted=newest')

    expect(cheerio.load(response.text)('.govuk-notification-banner')).toHaveLength(0)
  })

  it('escapes the alias', async () => {
    const response = await request(app()).get('/?deleted=%3Cscript%3E')

    expect(response.text).toContain('The short URL &lt;script&gt; was deleted')
    expect(response.text).not.toContain('<script>alert')
  })
})

describe('security headers', () => {
  it('allow only the page’s own scripts and styles, and inline scripts with its nonce', async () => {
    const response = await request(app()).get('/')

    const csp = response.headers['content-security-policy'] ?? ''
    const nonce = /'nonce-([^']+)'/.exec(csp)?.[1]
    expect(nonce).toBeDefined()
    expect(csp).toMatch(/script-src 'self' 'nonce-[^']+'/)
    expect(csp).toContain("style-src 'self'")
    expect(csp).toContain("frame-ancestors 'none'")
    const $ = cheerio.load(response.text)
    expect($('script').map((_, script) => $(script).attr('nonce')).get()).toEqual([nonce, nonce])
  })

  it('use a fresh nonce for every response', async () => {
    const first = await request(app()).get('/')
    const second = await request(app()).get('/')

    expect(first.headers['content-security-policy']).not.toBe(second.headers['content-security-policy'])
  })

  it('include the other protections', async () => {
    const response = await request(app()).get('/')

    expect(response.headers['x-content-type-options']).toBe('nosniff')
    expect(response.headers['referrer-policy']).toBe('same-origin')
    expect(response.headers['strict-transport-security']).toBeDefined()
    expect(response.headers['x-powered-by']).toBeUndefined()
  })
})

describe('assets', () => {
  it.each([
    ['/assets/govuk-frontend.min.css', 'text/css'],
    ['/assets/govuk-frontend.min.js', 'text/javascript'],
  ])('serves %s', async (path, type) => {
    const response = await request(app()).get(path)

    expect(response.status).toBe(200)
    expect(response.type).toBe(type)
  })

  it('serves GOV.UK Frontend’s CSS without the font', async () => {
    const response = await request(app()).get('/assets/govuk-frontend.min.css')

    expect(response.text).toContain('.govuk-button')
    expect(response.text).not.toMatch(/@font-face|\/assets\/fonts\//)
  })

  it.each(['/assets/fonts/bold-b542beb274-v2.woff2', '/assets/images/govuk-crest.svg'])(
    'does not serve %s',
    async (path) => {
      const response = await request(app()).get(path)

      expect(response.status).toBe(404)
    }
  )
})

describe('errors', () => {
  it('shows a not found page for an unknown path', async () => {
    const response = await request(app()).get('/no-such-page')

    expect(response.status).toBe(404)
    expect(cheerio.load(response.text)('h1').text()).toBe('Page not found')
  })

  it('shows a page that gives nothing away for an unexpected fault', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined)
    api.list = () => Promise.reject(new Error('Secret internal detail'))

    const response = await request(app()).get('/?created=x')

    expect(response.status).toBe(500)
    expect(cheerio.load(response.text)('h1').text()).toBe('Sorry, there is a problem with the service')
    expect(response.text).not.toContain('Secret internal detail')
    expect(consoleError).toHaveBeenCalled()
    consoleError.mockRestore()
  })
})
