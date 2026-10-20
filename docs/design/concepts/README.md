# Cross-cutting concepts

Cross-cutting concepts are concerns that apply across the whole system, rather
than to one architectural view or one component.

This view is for developers who need to know the system-wide rule for something
— eg. what an alias is, how errors look, what gets logged — before changing code
that touches it.

## Contents

- **[Domain model](./domain-model.md)** \
  Aliases, destination URLs, short URLs, and how they relate.

- **[Persistence](./persistence.md)** \
  The schema, the guarantees the database provides, and migrations.

- **[Error handling](./error-handling.md)** \
  The API service's problem-details format, and how the web client presents errors.

- **[Configuration](./configuration.md)** \
  Environment variables, the two base URLs, and the secrets that security
  depends on.

- **[Scalability and resilience](./scalability-and-resilience.md)** \
  Expected traffic pattern, and the scalability and resilience measures built into the MVP.
