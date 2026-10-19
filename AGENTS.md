# [Project Name]

A short paragraph describing what this project does, who it is for, and any
constraints that materially affect how AI agents should approach changes to it.

The capitalized words REQUIRED, MUST, MUST NOT, RECOMMENDED, SHOULD,
SHOULD NOT, OPTIONAL, and MAY are to be interpreted as described in
[IETF RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

## Team

- **[Name], Product Owner.** \
  Owns the requirements specification and prioritizes the backlog. First
  point of contact for scope questions.

- **[Name], Project Lead.** \
  Owns overall direction, priorities, and scope trade-offs. Final
  decision-maker when requirements conflict.

- **[Name], Tech Lead / Architect.** \
  Owns architecture decisions, design docs, and RFCs. Reviews significant
  structural changes.

- **[Name], Security Lead.** \
  Runs threat modeling sessions, owns the risk register, and reviews changes
  to authentication, authorization, and data handling.

- **[Name], QA / Test Lead.** \
  Owns test strategy and the automated test suite. Signs off on acceptance
  testing before release.

- **[Name], Release Manager.** \
  Owns the release process and versioning. Decides when a release branch is
  cut and what ships in it.

- **[Name], Documentation Owner.** \
  Maintains developer and maintainer docs, including keeping architectural
  decision records up to date.

## Tech stack

- Language and runtime versions.
- Major frameworks and libraries.

## Project structure

- [`src/`](./src/) \
  Application source.

- [`tests/`](./tests/) \
  Automated tests (unit, integration, system).

- [`run/`](./run) \
  Dev tools (Bash scripts).

- [`docs/`](./docs/) \
  Developer/maintainer docs, including architectural decision records.

- [`.agents/skills/`](./.agents/skills/) \
  On-demand context for agents.

## Documentation

- **Software requirements specification (SRS)** \
  [`docs/specs/`](./docs/specs/) (mono-repo) | <https://github.com/kieranpotts/specs> (multi-repo)

- **Design docs** \
  [`docs/design/`](./docs/design/) (mono-repo) | <https://github.com/kieranpotts/design> (multi-repo)

- **Delivery plans** \
  [`docs/plans/`](./docs/plans/) (mono-repo) | <https://github.com/kieranpotts/plans> (multi-repo)

- **Architecture decision records (ADRs)** \
  [`docs/decisions/`](./docs/decisions/) (mono-repo) | <https://github.com/kieranpotts/rfc> (multi-repo)

- **Architecture audit reports** \
  [`docs/audits/`](./docs/audits/) (mono-repo) | <https://github.com/kieranpotts/audits> (multi-repo)

- **Risk register** \
  [`docs/risks/`](./docs/risks/) (mono-repo) | <https://github.com/kieranpotts/risks> (multi-repo)

## Tools

The following commands are available to automate development lifecycle steps.

- `command` \
  Build production-grade artifacts.

- `command` \
  Runs the linter.

- `command` \
  Runs the automated test suite.

## Utilities

Besides standard Debian utilities available in the bookworm-slim base image,
the following programs are installed in your shell environment.

- `codespell`
- `curl`
- `ec` (editorconfig-checker)
- `gh` (GitHub CLI)
- `git` and `git-lfs`
- `gpg` (GnuPG)
- `jq`
- `make`
- `nvm` and `node`
- `pre-commit`
- `python3`
- `rg` (ripgrep)
- `shellcheck`
- `skills-ref`
- `tar`
- `tmux`
- `unzip`
- `wget`

## Rules

- The root [`README`](./README.md) is for _users_ of the project. It covers
  requirements, installation, and usage. Material for developers and
  maintainers MUST NOT be included here. That belongs in
  [`CONTRIBUTING.md`](./CONTRIBUTING.md).

- MUST NOT do this.

- SHOULD do this.

- MAY do this.

## Skills

The [`.agents/skills/`](./.agents/skills/) directory provides on-demand skills
for managing this repository. See the [`README`](./.agents/skills/README.md) in
that directory for descriptions of the available skills and the situations
when you should use them.

`.claude/skills` is a symlink to `.agents/skills` for auto-discovery by Claude
Code.

## References

The following technical standards (TS) govern this project. Fetch and ingest
the relevant standards as-and-when required for the task at hand.

(Delete from the template those that don't apply.)

- [**TS-1: Software Requirements Specification**](https://kieranpotts.com/standards/001) \
  Use when writing, reviewing, or evaluating a software requirements
  specification (SRS), acceptance criteria, Gherkin feature files,
  non-functional qualities, or the lifecycle of product proposals.

- [**TS-2: Software Design Philosophy**](https://kieranpotts.com/standards/002) \
  Use when evaluating a software design or guiding an architectural decision.

- [**TS-3: Design Docs**](https://kieranpotts.com/standards/003) \
  Use when writing, reviewing, or maintaining design docs, RFCs, architecture
  decision records (ADRs), or architecture audit reports.

- [**TS-4: Modeling**](https://kieranpotts.com/standards/004) \
  Use when defining architectural views (conceptual, logical, development,
  process, physical, etc.) or choosing tools/notations for system modeling.

- [**TS-5: Application Architecture**](https://kieranpotts.com/standards/005) \
  Use when designing or reviewing the architecture of a standalone application,
  library, service, or microservice.

- [**TS-6: Distributed System Design**](https://kieranpotts.com/standards/006) \
  Use when designing or reviewing a system distributed across multiple
  processes, services, or machines.

- [**TS-7: Code Design**](https://kieranpotts.com/standards/007) \
  Use when writing or reviewing low-level code structure. Covers naming,
  abstraction, decomposition, comments, error handling, and layout.
  Language-agnostic.

- [**TS-8: Issue Tracking**](https://kieranpotts.com/standards/008) \
  Use when creating, triaging, or reviewing issues in an issue tracker, or when
  designing issue-tracking workflows and boards.

- [**TS-9: Version Control**](https://kieranpotts.com/standards/009) \
  Use when working with Git. Covers commits, branching, merging, integration
  strategies, cutting releases, and configuring Git/PR/CI tooling.

- [**TS-10: Releasing**](https://kieranpotts.com/standards/010) \
  Use when choosing release cadence or rollout strategy, planning rollback, or
  writing release notes, changelogs, or deprecation notices.

- [**TS-11: Versioning**](https://kieranpotts.com/standards/011) \
  Use when choosing a versioning scheme, formatting version strings, or tagging
  releases (SemVer, CalVer).

- [**TS-12: Quality Assurance**](https://kieranpotts.com/standards/012) \
  Use when designing or reviewing the QA process. Covers quality culture, the
  Definition of Done, code review, quality gates, and quality metrics.

- [**TS-13: Functional Testing**](https://kieranpotts.com/standards/013) \
  Use when designing, writing, or reviewing functional tests. Covers test
  strategy, types, levels, coverage, test doubles, and TDD.

- [**TS-14: Quality Attribute Testing**](https://kieranpotts.com/standards/014) \
  Use when testing non-functional requirements. Covers testing of performance
  (latency and throughout) testing, load testing, accessibility testing,
  compliance testing, security testing, and more.

- [**TS-15: User Interfaces**](https://kieranpotts.com/standards/015) \
  Use when designing any human-computer interface (GUI, TUI, CLI, or API) at a
  level general to all interface types.

- [**TS-16: Command Line Interfaces (CLIs)**](https://kieranpotts.com/standards/016) \
  Use when designing, writing, or reviewing command line interfaces. Covers
  option flags, subcommands, piping, exit codes, output, and errors.

- [**TS-17: Graphical User Interfaces (GUIs)**](https://kieranpotts.com/standards/017) \
  Use when designing or reviewing a graphical user interface in general
  (non-web-specific).

- [**TS-18: Web GUIs**](https://kieranpotts.com/standards/018) \
  Use when designing or implementing web-based GUIs. Covers performance
  optimization, web accessibility (WCAG), and web font handling.

- [**TS-19: Search Engine Optimization (SEO)**](https://kieranpotts.com/standards/019) \
  Use when creating or optimizing content for search engine ranking.

- [**TS-20: Network APIs**](https://kieranpotts.com/standards/020) \
  Use when designing or reviewing a network API of any kind (REST, GraphQL,
  gRPC, WebSocket). Covers inter-service communication, reliability, security,
  and observability.

- [**TS-21: HTTP APIs**](https://kieranpotts.com/standards/021) \
  Use when designing or reviewing an HTTP API. Covers RESTful style, HTTP
  methods, status codes, resources, and versioning.

- [**TS-22: Webhooks**](https://kieranpotts.com/standards/022) \
  Use when designing or implementing webhooks, as producer or consumer.

- [**TS-23: Messages and Events**](https://kieranpotts.com/standards/023) \
  Use when designing or implementing messages and events in an
  intra-organization message-driven architecture.

- [**TS-24: User Manuals**](https://kieranpotts.com/standards/024) \
  Use when writing or reviewing end-user manuals or product documentation.

- [**TS-25: Technical Documentation**](https://kieranpotts.com/standards/025) \
  Use when deciding what documentation a project needs, where it should live,
  who it's for, or whether it's still trustworthy.

- [**TS-26: Technical Writing Style Guide**](https://kieranpotts.com/standards/026) \
  Use when writing or editing the prose of a technical document. Covers
  tone-of-voice, headings, terminology, lists, and citations.

- [**TS-27: Markdown**](https://kieranpotts.com/standards/027) \
  Use when writing or reviewing Markdown documents. Covers READMEs, changelogs,
  PR descriptions, and issue comments.

- [**TS-28: AsciiDoc**](https://kieranpotts.com/standards/028) \
  Use when writing or reviewing AsciiDoc documents or websites built using
  Antora.

- [**TS-29: JSON Schema**](https://kieranpotts.com/standards/029) \
  Use when designing or using JSON Schema. Covers validation, `$ref`, OpenAPI,
  JSON-LD, and JSON Pointer.

- [**TS-30: YAML**](https://kieranpotts.com/standards/030) \
  Use when writing or reviewing YAML files.

- [**TS-31: Unix Shells and POSIX Standards**](https://kieranpotts.com/standards/031) \
  Use when authoring or modifying shell scripts that must be POSIX-compliant and
  run across multiple shells (sh, bash, zsh, dash) and platforms.

- [**TS-32: Bash**](https://kieranpotts.com/standards/032) \
  Use when authoring or modifying scripts that target Bash specifically, and
  which use Bash extensions ("Bashisms").

- [**TS-33: Java**](https://kieranpotts.com/standards/033) \
  Use when writing or reviewing Java code. Covers source organization, naming,
  style, constructs, and Javadoc.

- [**TS-34: PHP**](https://kieranpotts.com/standards/034) \
  Use when writing, reviewing, or refactoring PHP code.

- [**TS-35: Python**](https://kieranpotts.com/standards/035) \
  Use when writing, reviewing, or refactoring Python code.

- [**TS-36: ECMAScript (JavaScript/TypeScript)**](https://kieranpotts.com/standards/036) \
  Use when writing or reviewing JavaScript or TypeScript source code. Covers
  syntax, modules, async programming, functional patterns, and testing.

- [**TS-37: Web Platform APIs**](https://kieranpotts.com/standards/037) \
  Use when working with browser/runtime web platform APIs and the HTTP protocol,
  distinct from a specific language or framework.

- [**TS-38: Node.js Applications**](https://kieranpotts.com/standards/038) \
  Use when designing, building, or deploying Node.js applications.

- [**TS-39: HTML**](https://kieranpotts.com/standards/039) \
  Use when writing or reviewing HTML markup.

- [**TS-40: CSS**](https://kieranpotts.com/standards/040) \
  Use when writing or reviewing CSS. Covers class naming conventions, separating
  layout from content, and building reusable components.

- [**TS-41: React**](https://kieranpotts.com/standards/041) \
  Use when writing, reviewing, or refactoring React component code.

- [**TS-42: Vue**](https://kieranpotts.com/standards/042) \
  Use when writing, reviewing, or refactoring Vue component code.

- [**TS-43: Relational Databases and SQL**](https://kieranpotts.com/standards/043) \
  Use when designing schemas or writing SQL for a relational database.

- [**TS-44: Non-Relational (NoSQL) Databases**](https://kieranpotts.com/standards/044) \
  Use when designing schemas or working with a non-relational (NoSQL) database.

- [**TS-45: Data Migrations**](https://kieranpotts.com/standards/045) \
  Use when planning, executing, validating, or rolling back a data migration.

- [**TS-46: Distributed Data and Caching**](https://kieranpotts.com/standards/046) \
  Use when designing distributed data or caching strategies. Covers consistency,
  replication, and cache placement.

- [**TS-47: Dates and Times**](https://kieranpotts.com/standards/047) \
  Use when designing or implementing systems that store, transmit, or display
  date and time values.

- [**TS-48: Environment Variables**](https://kieranpotts.com/standards/048) \
  Use when designing or implementing application configuration via environment
  variables.

- [**TS-49: Cloud Platform Engineering**](https://kieranpotts.com/standards/049) \
  Use when designing or building self-service internal cloud platforms for
  development teams.

- [**TS-50: Cloud Economics**](https://kieranpotts.com/standards/050) \
  Use when making strategic or architectural decisions about cloud spending,
  auto-scaling cost controls, or cloud-vs-dedicated-server trade-offs.

- [**TS-51: Amazon Web Services (AWS)**](https://kieranpotts.com/standards/051) \
  Use when designing, provisioning, or governing AWS resources.

- [**TS-52: Security and Secrets Management**](https://kieranpotts.com/standards/052) \
  Use when designing, implementing, or reviewing application security,
  authentication, authorization, or secrets handling.

- [**TS-53: Privacy and Data Protection**](https://kieranpotts.com/standards/053) \
  Use when designing, implementing, or reviewing systems that collect, store, or
  process personal data (PII, GDPR, HIPAA, PCI).

- [**TS-54: Threat Modeling**](https://kieranpotts.com/standards/054) \
  Use when designing, reviewing, or iterating on a system's security posture.
  Covers threat modeling workshops and maintenance of a project's risk register.

- [**TS-55: Authentication and Authorization**](https://kieranpotts.com/standards/055) \
  Use when designing, implementing, or reviewing authentication and
  authorization mechanisms.

- [**TS-56: JSON Web Tokens (JWTs)**](https://kieranpotts.com/standards/056) \
  Use when designing, implementing, or reviewing JWT-based authentication or
  authorization.

- [**TS-57: Logging, Monitoring, Observability**](https://kieranpotts.com/standards/057) \
  Use when designing or implementing logging, monitoring, alerting, or metrics
  collection.

- [**TS-58: Docker**](https://kieranpotts.com/standards/058) \
  Use when designing Dockerfiles, building Docker images, or running Docker
  containers.

- [**TS-59: Terraform**](https://kieranpotts.com/standards/059) \
  Use when authoring Terraform/HCL configurations or managing
  infrastructure-as-code lifecycles.

- [**TS-60: GitHub Actions**](https://kieranpotts.com/standards/060) \
  Use when designing, authoring, reviewing, or securing GitHub Actions workflows
  or custom actions.

- [**TS-61: AI Tools**](https://kieranpotts.com/standards/061) \
  Use when planning or executing coding tasks, managing your own context,
  authoring AGENTS.md files or agent skills, calling tools, or handling
  untrusted content.

- [**TS-62: Make**](https://kieranpotts.com/standards/062) \
  Use when authoring or modifying `Makefile`s, including project task runners
  built on GNU Make.

- [**TS-63: URL Design**](https://kieranpotts.com/standards/063) \
  Use when designing or reviewing the URL structure of any HTTP service.
