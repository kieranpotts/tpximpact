# Physical view

This architectural view describes where the system runs: the environments, the
infrastructure in each, and the network boundaries.

This view is for anyone running the system locally, or reasoning about it in
production. The [process view](../process/) describes what is running in each
deployed container.

There are two environments:

- **Local**, which runs entirely in Docker Compose from `compose.yaml`. This is
  the environment this repository actually builds and runs.

- **Production**, a reference deployment on AWS. This repository doesn't
  provision it. A separate Terraform repository would. It's documented here as
  the target the images and configuration are designed for.

The same two images run in both. Only environment variables differ.

## Local

**Developer machines** require only Docker. No JDK or Node.js runtime is
needed to run the system on the host machine.

The local system consists of three Docker Compose services: `web` and `api` are
built from `./src/web-client` and `./src/api-service` respectively; `db` is the
official `postgres:18` image.

Theres's a named Docker volume holding PostgreSQL's data directory. It survives
container restarts and `docker compose down`.

```mermaid
C4Deployment
  title Local environment

  Person(dev, "Developer", "Browser or curl")

  Deployment_Node(host, "Developer machine", "Docker") {
    Deployment_Node(net, "Compose network", "Docker bridge network") {
      Deployment_Node(webc, "web", "Docker container") {
        Container(web, "Web client", "Node.js 24, Express 5", "Listens on :3000")
      }
      Deployment_Node(apic, "api", "Docker container") {
        Container(api, "API service", "Java 25, Kotlin, Ktor 3", "Listens on :8080")
      }
      Deployment_Node(dbc, "db", "Docker container, postgres:18", "Data on the db-data named volume") {
        ContainerDb(db, "Database", "PostgreSQL 18", "Listens on :5432")
      }
    }
  }

  Rel(dev, web, "Uses", "localhost:3000")
  Rel(dev, api, "Follows short URLs, calls the API service", "localhost:8080")
  Rel(web, api, "Calls", "http://api:8080")
  Rel(api, db, "Reads and writes", "db:5432")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="3")
```

| Service | Reachable at                             | Notes                                     |
| ------- | ---------------------------------------- | ----------------------------------------- |
| `web`   | `http://localhost:3000`                  | The web client.                             |
| `api`   | `http://localhost:8080`                  | The API service, and the prefix of every short URL. |
| `db`    | Not published                            | Data is on the `db-data` named volume.    |

`web` reaches the API service at `http://api:8080` on the Compose network. The
API service builds short URLs with `PUBLIC_BASE_URL=http://localhost:8080`, the
address the browser of a link editor or short URL visitor can reach.

## Production

> [!IMPORTANT]
> The production environment is not implemented at this time. It is documented
> here as a reference for the target production infrastructure.

This is a minimal deployment design for production, on AWS. It shows only the
infrastructure the system cannot run without. Edge protection (CDN, WAF, DNS),
observability, secrets management, and the delivery pipeline are out of scope
here.

Two public hostnames are needed. The short domain (eg. `sho.rt`) serves the API
service. The app domain (eg. `app.sho.rt`) serves the web client.

```mermaid
C4Deployment
  title Production environment (minimal reference)

  Person(users, "Link editors, short URL visitors, and API clients", "People and programs")

  Deployment_Node(aws, "AWS account", "eu-west-2") {
    Deployment_Node(vpc, "VPC", "3 Availability Zones") {
      Deployment_Node(public, "Public subnets", "Subnet tier") {
        System_Ext(alb, "Application Load Balancer", "Routes by Host header")
      }
      Deployment_Node(private, "Private subnets", "Subnet tier") {
        Deployment_Node(websvc, "ECS service: web", "Fargate, at least 2 tasks") {
          Container(web, "Web client", "Node.js 24, Express 5")
        }
        Deployment_Node(apisvc, "ECS service: api", "Fargate, at least 2 tasks") {
          Container(api, "API service", "Java 25, Kotlin, Ktor 3")
        }
      }
      Deployment_Node(isolated, "Isolated subnets", "Subnet tier") {
        Deployment_Node(rds, "RDS for PostgreSQL", "Multi-AZ") {
          ContainerDb(db, "Database", "PostgreSQL 18")
        }
      }
    }
  }

  Rel(users, alb, "Sends requests", "HTTPS")
  Rel(alb, web, "Host: app domain")
  Rel(alb, api, "Host: short domain")
  Rel(web, api, "Calls", "Service Connect")
  Rel(api, db, "Reads and writes", "JDBC")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="2")
```

The load balancer is the only component with a public address. It routes by
`Host` header to the `web` or `api` service. Health checks can be configured
here.

The `web` and `api` images run as ECS Fargate services, with at least two tasks
each, spread across three Availability Zones. `web` calls `api` through ECS
Service Connect, inside the VPC.

The database is RDS for PostgreSQL, with a synchronous standby in another
Availability Zone, automated backups, point-in-time recovery, and encryption at
rest. It sits in isolated subnets that have no route to the internet.

Sizing, network CIDRs, and IAM policies belong in the infrastructure repository
— out-of-scope for this repository.
