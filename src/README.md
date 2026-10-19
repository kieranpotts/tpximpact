# Source

Everything that ships. One subdirectory per independently-deployable application.

- [**`api-service/`**](./api-service/) \
  The URL shortener API, written in Kotlin with Ktor. It's a self-contained
  Gradle build, and it builds to a container image. It owns the API contract.

- [**`web-client/`**](./web-client/) \
  The web frontend, written in TypeScript with Express. It's a self-contained
  npm package, and it builds to a container image. It's a client of
  `api-service`.
