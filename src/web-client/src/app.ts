import express, { type Express } from "express";

// Placeholder, until the web client has real pages.
const homePage = `<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>URL shortener</title>
  </head>
  <body>
    <h1>URL shortener</h1>
  </body>
</html>
`;

export function createApp(): Express {
  const app = express();
  app.disable("x-powered-by");

  app.get("/", (_req, res) => {
    res.type("html").send(homePage);
  });

  return app;
}
