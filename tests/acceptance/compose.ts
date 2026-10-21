import { execFileSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const composeFile = fileURLToPath(new URL('../../compose.yaml', import.meta.url))

/**
 * Runs a `docker compose` command on the system under test, and waits for it
 * to finish. The Compose project is the one that `COMPOSE_PROJECT_NAME`
 * names, as `run/acceptance` sets it, or else the one that `run/up` starts.
 *
 * Only tests in the `disruptive` project may stop or start containers. See
 * `playwright.config.ts`.
 */
export function compose (...args: string[]) {
  execFileSync('docker', ['compose', '--file', composeFile, ...args], {
    stdio: ['ignore', 'pipe', 'pipe'],
  })
}

/** Starts the given services, if they're stopped, and waits until they're healthy. */
export function startAndWait (...services: string[]) {
  compose('up', '--detach', '--wait', '--no-recreate', ...services)
}
