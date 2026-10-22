import { HttpShortenerApi } from './api/http-shortener-api.js'
import { createApp } from './app.js'
import { ConfigError, loadConfig } from './config.js'

const port = 3000

// How long in-flight requests get to finish, after a shutdown signal.
const shutdownGraceMs = 10_000

// How long a call to the API service may take before it counts as unavailable.
const apiTimeoutMs = 3_000

function configOrExit () {
  try {
    return loadConfig()
  } catch (error) {
    if (error instanceof ConfigError) {
      console.error(error.message)
      process.exit(1)
    }
    throw error
  }
}

const config = configOrExit()
const api = new HttpShortenerApi(config.apiBaseUrl, apiTimeoutMs)

// Express 5 calls back with an error, eg. `EADDRINUSE`, if it can't listen.
const server = createApp({ api }).listen(port, (error) => {
  if (error) {
    throw error
  }
  console.log(`Web client listening on port ${String(port)}`)
})

function shutDown (signal: NodeJS.Signals): void {
  console.log(`Received ${signal}, shutting down`)
  server.close(() => process.exit(0))
  server.closeIdleConnections()
  setTimeout(() => process.exit(1), shutdownGraceMs).unref()
}

process.once('SIGTERM', shutDown)
process.once('SIGINT', shutDown)
