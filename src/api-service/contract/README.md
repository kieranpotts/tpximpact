# Contract

The interface contract between the API and its clients.

- [**`openapi.yaml`**](./openapi.yaml) \
  The URL shortener's HTTP API, as an OpenAPI 3.0 document. It is the source of
  truth for the API's behavior. It is served publicly at `GET /openapi.yaml`,
  and the acceptance tests are derived from it.
