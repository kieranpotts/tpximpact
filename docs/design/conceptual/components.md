# Components

The system is a client-server architecture with three main components.

- A **web client** application, used by people for link creation. People can
  also use the web client to list and delete existing short URLs that they and
  others have previously created. The application is a client of the API service.
  It turns HTML form submissions into API service calls. It persists no data of
  its own.

- An **API service**, which is responsible for validating input, generating
  aliases, storing and deleting URL mappings, listing them, and redirecting
  short URL visitors. It's the single authority for every business rule. API
  clients, including the web client and short URL visitors, interact with the
  API service directly.

- A single **database** for persistence, which only the API service can read
  or write. It durably stores a mapping of every alias to its destination URL.

The system has no other external dependencies.

The system does not validate the existence of, or interact in any way with,
destination URLs. Only a short URL visitor's browser interacts with a
destination URL, following a redirect from an alias.

The client-server interface is defined in the OpenAPI interface definition
language. The API service publishes the interface specification at
`/openapi.yaml`.
