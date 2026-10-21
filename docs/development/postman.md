# Testing the API with Postman

[Postman](https://www.postman.com/) can build a ready-made collection from the
API's OpenAPI contract.

The API service publishes its contract at `GET /openapi.yaml`. Start the system
locally:

```sh
make up
```

Then download the OpenAPI contract. Run this from your host machine (change
the port number if you're using a different one to serve the API service).

```sh
curl -o openapi.yaml http://localhost:8080/openapi.yaml
```

In Postman, choose **Import**, then drop in the downloaded `openapi.yaml` file.
Accept the defaults: generate a **Collection** with **Request** bodies and
parameters from the schema.

The contract doesn't declare a server, so the imported requests have no host.
Set one once, for the whole collection:

1. Open the collection, then its **Variables** tab.

2. Set `baseUrl` to `http://localhost:8080` (or your `API_PORT`), and save.

Every response carries an `X-Request-Id` header. To correlate a request with
the service's logs (`make logs`), send your own `X-Request-Id` header.
