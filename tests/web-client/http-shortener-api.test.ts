import { createServer, type IncomingMessage, type Server, type ServerResponse } from 'node:http'
import type { AddressInfo } from 'node:net'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { HttpShortenerApi } from '../../src/web-client/src/api/http-shortener-api.js'

/** What the stub API service received. */
interface Received {
  method: string | undefined
  url: string | undefined
  contentType: string | undefined
  body: string
}

/**
 * A local stub of the API service. Each test sets how it answers, and it
 * records what it receives.
 */
let respond: (res: ServerResponse) => void
let received: Received[]
let server: Server
let baseUrl: string

beforeEach(async () => {
  received = []
  respond = (res) => res.end()
  server = createServer((req: IncomingMessage, res) => {
    let body = ''
    req.on('data', (chunk: Buffer) => { body += chunk.toString() })
    req.on('end', () => {
      received.push({
        method: req.method,
        url: req.url,
        contentType: req.headers['content-type'],
        body,
      })
      respond(res)
    })
  })
  await new Promise<void>((resolve) => server.listen(0, '127.0.0.1', resolve))
  baseUrl = `http://127.0.0.1:${String((server.address() as AddressInfo).port)}`
})

afterEach(async () => {
  server.closeAllConnections()
  await new Promise((resolve) => server.close(resolve))
})

function json (status: number, body: unknown, contentType = 'application/json') {
  return (res: ServerResponse) => {
    res.writeHead(status, { 'Content-Type': contentType }).end(JSON.stringify(body))
  }
}

const validationFailed = {
  type: 'urn:problem:url-shortener:validation-failed',
  title: 'The request is not valid',
  status: 400,
  detail: 'The request has 1 invalid field.',
  errors: [
    {
      field: 'destinationUrl',
      code: 'url-unsupported-scheme',
      message: 'Enter a URL starting with http:// or https://',
    },
  ],
}

describe('HttpShortenerApi.shorten', () => {
  it('posts the request as JSON, and returns the short URL', async () => {
    respond = json(201, { shortUrl: 'http://localhost:8080/my-alias' })

    const result = await new HttpShortenerApi(baseUrl, 1_000).shorten({
      destinationUrl: 'https://example.com/',
      customAlias: 'my-alias',
    })

    expect(result).toEqual({ kind: 'ok', value: { shortUrl: 'http://localhost:8080/my-alias' } })
    expect(received).toEqual([
      {
        method: 'POST',
        url: '/shorten',
        contentType: 'application/json',
        body: '{"destinationUrl":"https://example.com/","customAlias":"my-alias"}',
      },
    ])
  })

  it('returns a rejection with its problem details', async () => {
    respond = json(400, validationFailed, 'application/problem+json')

    const result = await new HttpShortenerApi(baseUrl, 1_000).shorten({ destinationUrl: 'x' })

    expect(result).toEqual({ kind: 'rejected', problem: validationFailed })
  })

  it.each([
    ['not JSON', (res: ServerResponse) => res.writeHead(400).end('Bad request')],
    ['not a problem', json(400, { error: 'bad' })],
    ['a problem with malformed errors', json(400, { ...validationFailed, errors: [{ field: 1 }] })],
  ])('returns a rejection without a problem when the body is %s', async (_, response) => {
    respond = response

    const result = await new HttpShortenerApi(baseUrl, 1_000).shorten({ destinationUrl: 'x' })

    expect(result).toEqual({ kind: 'rejected' })
  })

  it.each([
    ['a 5xx', json(503, { type: 'urn:problem:url-shortener:service-unavailable', title: 'x', status: 503 })],
    ['a 2xx that is not JSON', (res: ServerResponse) => res.writeHead(201).end('Created')],
    ['a 2xx without a short URL', json(201, { url: 'http://localhost:8080/x' })],
    ['a redirect', (res: ServerResponse) => res.writeHead(302, { Location: '/elsewhere' }).end()],
  ])('returns unavailable for %s', async (_, response) => {
    respond = response

    const result = await new HttpShortenerApi(baseUrl, 1_000).shorten({ destinationUrl: 'x' })

    expect(result).toEqual({ kind: 'unavailable' })
  })

  it('returns unavailable when the API service does not answer in time', async () => {
    respond = () => { /* Never answers. */ }

    const started = Date.now()
    const result = await new HttpShortenerApi(baseUrl, 100).shorten({ destinationUrl: 'x' })

    expect(result).toEqual({ kind: 'unavailable' })
    expect(Date.now() - started).toBeLessThan(1_000)
  })

  it('returns unavailable when the API service cannot be reached', async () => {
    server.closeAllConnections()
    await new Promise((resolve) => server.close(resolve))
    server = createServer()

    const result = await new HttpShortenerApi(baseUrl, 1_000).shorten({ destinationUrl: 'x' })

    expect(result).toEqual({ kind: 'unavailable' })
  })
})

describe('HttpShortenerApi.list', () => {
  it('gets every short URL', async () => {
    const shortUrls = [
      { alias: 'abc', destinationUrl: 'https://example.com/', shortUrl: 'http://localhost:8080/abc' },
    ]
    respond = json(200, shortUrls)

    const result = await new HttpShortenerApi(baseUrl, 1_000).list()

    expect(result).toEqual({ kind: 'ok', value: shortUrls })
    expect(received).toMatchObject([{ method: 'GET', url: '/urls' }])
  })

  it('returns unavailable when the list is malformed', async () => {
    respond = json(200, [{ alias: 'abc' }])

    const result = await new HttpShortenerApi(baseUrl, 1_000).list()

    expect(result).toEqual({ kind: 'unavailable' })
  })
})
