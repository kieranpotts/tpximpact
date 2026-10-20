# Web client components

The web client is an Express application in TypeScript. It has no business
rules. It merely gathers input, calls the API service, and presents the outcome.
It doesn't duplicate the API service's validation, so the rules exist in one
place.

```mermaid
C4Component
  title Web client: components

  Person(browser, "Browser", "A link editor's browser")

  Container_Boundary(webc, "Web client") {
    Boundary(l2, "Presentation", "layer") {
      Component(views, "Nunjucks views", "GOV.UK Frontend", "Pages and partials")
      Component(presenter, "Error presenter", "TypeScript", "Turns API errors into error view models")
    }
    Boundary(l3, "API access", "layer") {
      Component(apiif, "ShortenerApi", "TypeScript interface", "shorten, list, remove")
      Component(httpclient, "HttpShortenerApi", "fetch", "Implements ShortenerApi")
    }
    Boundary(l1, "Handlers", "layer") {
      Component(routes, "Route handlers", "Express", "Gather input and present outcomes")
    }
  }

  Boundary(upstream, "Upstream", "system") {
    Container_Ext(api, "API service", "Kotlin, Ktor", "Owns every business rule")
  }

  Rel(browser, routes, "Uses")
  Rel(routes, views, "Renders")
  Rel(routes, presenter, "Presents errors with")
  Rel(routes, apiif, "Calls")
  Rel(httpclient, apiif, "Implements")
  Rel(httpclient, api, "Calls")

  UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="2")
```

Route handlers handle form posts and page loads. Each handler branches on the
API client's result (`ok`, `rejected`, or `unavailable`). Pages are rendered
from Nunjucks views built with GOV.UK Frontend macros.

Handlers depend on the `ShortenerApi` interface, not on `fetch`, so they can be
tested against a fake. `HttpShortenerApi` is the production implementation. It
calls the API service over HTTP with a per-call timeout, and parses
problem-details error responses.
