# TPXimpact take-home programming project

**A URL shortener MVP in Kotlin and Express.** The system is an API service,
written in Kotlin with Ktor, and a decoupled server-rendered web client, served
by Express. Short URLs are stored in PostgreSQL.

**🚨🚨🚨 See also Kieran's project [📝 debriefing notes](./NOTES.md). 🚨🚨🚨**

## 🚀 Quick start

You need only **Docker**, with Docker Compose v2. From the repository root:

```sh
docker compose up --build
```

Then open the web client on <http://localhost:3000>. The API service is on
<http://localhost:8080>.

- [**Get started**](./docs/development/onboard.md) \
  Full documentation for building and running the system, and its tests, locally.

- [**Calling the API**](./docs/development/api.md) \
  Example requests for every endpoint.

## 📓 Developer documentation

- [**🛠️ Development tools and methods**](./docs/development/) \
  Local development configuration, and the day-to-day development workflow.

- [**📋 Requirements specification**](./docs/specs/) \
  Defines the behaviors and qualities of the system.

- [**📐 Design docs**](./docs/design/) \
  Architectural views of the system.

- [**📜 Architecture decision records**](./docs/decisions/) \
  An historical archive of key design and technical decisions.

- [**🗺️ Delivery plans**](./docs/plans/) \
  Decomposes delivery into independently shippable increments.

---

Copyright © 2026 Kieran Potts, all rights reserved.
