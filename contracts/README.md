# Contracts

The interface contracts shared by the API, the frontend, and the acceptance
tests.

- [**`openapi.yaml`**](./openapi.yaml) \
  The URL shortener's HTTP API, as an OpenAPI 3.0 document. It is the source of
  truth for the API's behavior. The API is tested against it, the frontend
  serves it, and the acceptance tests are derived from it. Copied verbatim from
  [`tpximpact/code-exercise-java@1c89ae2`](https://github.com/tpximpact/code-exercise-java/blob/1c89ae22cc4bde8298b0b41eefaf689e53155da9/openapi.yaml).
