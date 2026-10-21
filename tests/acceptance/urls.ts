// The tests reach the system only through its public URLs.

/** Where the web client is reachable. */
export const webClientUrl =
  process.env.WEB_CLIENT_URL ?? "http://localhost:3000";

/**
 * The public base URL: where the API service is reachable, and the prefix of
 * every short URL.
 */
export const publicBaseUrl =
  process.env.PUBLIC_BASE_URL ?? "http://localhost:8080";
