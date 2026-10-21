import { createApp } from "./app.js";

const port = 3000;

// How long in-flight requests get to finish, after a shutdown signal.
const shutdownGraceMs = 10_000;

// Express 5 calls back with an error, eg. `EADDRINUSE`, if it can't listen.
const server = createApp().listen(port, (error) => {
  if (error) {
    throw error;
  }
  console.log(`Web client listening on port ${String(port)}`);
});

function shutDown(signal: NodeJS.Signals): void {
  console.log(`Received ${signal}, shutting down`);
  server.close(() => process.exit(0));
  server.closeIdleConnections();
  setTimeout(() => process.exit(1), shutdownGraceMs).unref();
}

process.once("SIGTERM", shutDown);
process.once("SIGINT", shutDown);
