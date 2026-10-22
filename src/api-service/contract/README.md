# Contract

The interface contract between the API service and its clients.

- [**`openapi.yaml`**](./openapi.yaml) \
  The URL shortener's HTTP API, as an OpenAPI 3.0 document. It is the source of
  truth for the API service's behavior. It is served publicly at `GET /openapi.yaml`,
  and the API service's HTTP tests validate every response against it.
