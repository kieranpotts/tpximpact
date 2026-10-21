# API service components

The API service is a Kotlin application built on Ktor. Its code is arranged in
layers, with dependencies pointing inward, toward the domain. The layers are
packages in one Gradle module, and the dependency rule is enforced by
convention and code review.

```mermaid
C4Component
  title API service: layers

  Container_Ext(clients, "Clients", "Web client, curl, scripts", "Call the API service")

  Container_Boundary(apic, "API service") {
    Boundary(l1, "Inbound", "layer") {
      Component(http, "Inbound adapter", "Ktor", "HTTP routes and DTOs")
    }
    Boundary(l2, "Application", "layer") {
      Component(app, "Application", "Kotlin", "Use cases and the repository port")
    }
    Boundary(l3, "Core and outbound", "layers") {
      Component(domain, "Domain", "Kotlin", "Value types and validation rules")
      Component(jdbc, "Outbound adapter", "JDBC", "Implements the application's repository port")
    }
  }

  Boundary(storage, "Storage", "outside the API service") {
    ContainerDb_Ext(db, "Database", "PostgreSQL")
  }

  Rel(clients, http, "Calls")
  Rel(http, app, "Calls")
  Rel(app, domain, "Uses")
  Rel(jdbc, app, "Implements port of")
  Rel(jdbc, db, "Uses")

  UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

The application layer holds `ShortUrlService`, which implements all the use
cases of this service: shorten, resolve, delete, and list short URLs.

Adapters sit at the edge. The HTTP adapter turns requests into service calls
and results into responses. The persistence adapter runs SQL against
PostgreSQL through a HikariCP pool.

The composition root, `Application.kt`, is the only place that knows about
every layer. It reads configuration, constructs the components, and wires them
together by hand.

| Package  | Holds                                                                         |
| -------- | ----------------------------------------------------------------------------- |
| `http`   | The inbound adapter: routes, problem details, and request IDs.                |
| `jdbc`   | The outbound adapter: the connection pool, migrations, and the repository.    |
| `config` | Settings, read from the environment. See: concepts > configuration.           |
