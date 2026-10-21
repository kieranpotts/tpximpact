# API service components

The API service is a Kotlin application built on Ktor. Its code is arranged in
the four layers of domain-driven design (UI, application, domain, and
infrastructure), with dependencies pointing inward, toward the domain. The
layers are packages in one Gradle module, and the dependency rule is enforced
by convention and code review.

```mermaid
C4Component
  title API service: layers

  Container_Ext(clients, "Clients", "Web client, curl, scripts", "Call the API service")

  Container_Boundary(apic, "API service") {
    Boundary(l1, "UI", "layer") {
      Component(ui, "HTTP adapter", "Ktor", "HTTP routes and DTOs")
    }
    Boundary(l2, "Application", "layer") {
      Component(app, "Application", "Kotlin", "Use cases")
    }
    Boundary(l3, "Domain and infrastructure", "layers") {
      Component(domain, "Domain", "Kotlin", "Value types, validation rules, and the repository interface")
      Component(infra, "Persistence adapter", "JDBC", "Implements the domain's repository interface")
    }
  }

  Boundary(storage, "Storage", "outside the API service") {
    ContainerDb_Ext(db, "Database", "PostgreSQL")
  }

  Rel(clients, ui, "Calls")
  Rel(ui, app, "Calls")
  Rel(app, domain, "Uses")
  Rel(infra, domain, "Implements interface of")
  Rel(infra, db, "Uses")

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

| Package             | Layer          | Holds                                                                                    |
| ------------------- | -------------- | ---------------------------------------------------------------------------------------- |
| `ui`                | UI             | The HTTP adapter: routes, problem details, and request IDs.                              |
| `app`               | Application    | `ShortUrlService`, which implements the use cases.                                       |
| `domain`            | Domain         | Value types, business rules, and the repository interface. See: concepts > domain model. |
| `infra.config`      | Infrastructure | Settings, read from the environment. See: concepts > configuration.                      |
| `infra.persistence` | Infrastructure | The connection pool, migrations, and the JDBC repository.                                |
