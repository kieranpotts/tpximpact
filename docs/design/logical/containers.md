# Containers

The system has three containers (in the C4 sense): the **web client**, the
**API service**, and the **database**. Each container is built and deployed
independently.

```mermaid
C4Container
  title URL shortener: containers

  Person(browser, "Browser", "A link editor's or short URL visitor's browser")
  System_Ext(apiclient, "API client", "curl, scripts, other services")

  System_Boundary(system, "URL shortener") {
    Container(web, "Web client", "Node.js 24, Express 5, TypeScript", "Serves HTML pages and handles form posts")
    Container(api, "API service", "Java 25, Kotlin, Ktor 3", "Owns every business rule, and redirects short URLs")
    ContainerDb(db, "Database", "PostgreSQL 18", "Holds the short_url table")
  }

  Rel(browser, web, "Loads pages, submits forms", "HTML over HTTP")
  Rel(browser, api, "Follows short URLs", "GET /{alias}")
  Rel(apiclient, api, "Shortens, lists, and deletes URLs", "JSON over HTTP")
  Rel(web, api, "Shortens, lists, and deletes URLs", "JSON over HTTP")
  Rel(api, db, "Reads and writes mappings", "JDBC")

  UpdateRelStyle(browser, web, $offsetX="-160", $offsetY="-40")
  UpdateRelStyle(browser, api, $offsetX="-20", $offsetY="-70")
  UpdateRelStyle(web, api, $offsetX="-90", $offsetY="-60")

  UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```
