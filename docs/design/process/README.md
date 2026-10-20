# Process view

This architectural view describes how the system behaves at runtime: which
processes run, what they listen on, how they handle concurrent requests, how
they talk to each other, and how they start and stop.

This view is for anyone operating, debugging, or load-testing the system.

## Contents

- **[Overview](./overview.md)** \
  Processes, concurrency, communication, startup, shutdown, and failure behavior.

- **[Diagram](./diagram.md)** \
  The runtime structure of the processes.

- **[Elements](./elements.md)** \
  What each element in the diagram is responsible for.

- **[Notes](./notes.md)** \
  Connection limits and migration behavior to keep in mind.
