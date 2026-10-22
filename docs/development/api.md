# Calling the API

Start the system with `make up`, as [Get started](./onboard.md) describes.

## 📡 Example requests

These run against the API service on <http://localhost:8080>. Responses are
trimmed. Every response also carries an `X-Request-Id` header.

**Shorten a URL with a random alias:**

```sh
curl -i http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"destinationUrl": "https://www.gov.uk/browse/driving"}'
```

```http
HTTP/1.1 201 Created
Content-Type: application/json

{"shortUrl":"http://localhost:8080/RqEQrBI"}
```

**Shorten a URL with a custom alias:**

```sh
curl -i http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"destinationUrl": "https://www.tpximpact.com/", "customAlias": "tpx-readme"}'
```

```http
HTTP/1.1 201 Created
Content-Type: application/json

{"shortUrl":"http://localhost:8080/tpx-readme"}
```

**Follow a short URL:**

```sh
curl -i http://localhost:8080/tpx-readme
```

```http
HTTP/1.1 302 Found
Cache-Control: no-store
Location: https://www.tpximpact.com/
```

**List all short URLs, newest first:**

```sh
curl -i http://localhost:8080/urls
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

[{"alias":"tpx-readme","destinationUrl":"https://www.tpximpact.com/","shortUrl":"http://localhost:8080/tpx-readme"},
 {"alias":"RqEQrBI","destinationUrl":"https://www.gov.uk/browse/driving","shortUrl":"http://localhost:8080/RqEQrBI"}]
```

**Delete a short URL:**

```sh
curl -i -X DELETE http://localhost:8080/tpx-readme
```

```http
HTTP/1.1 204 No Content
```

**Get the API contract:**

```sh
curl -i http://localhost:8080/openapi.yaml
```

```http
HTTP/1.1 200 OK
Content-Type: application/yaml

openapi: 3.0.3
info:
  title: URL Shortener API
…
```

**A rejected request.** Errors are
[RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details, which name
each invalid field:

```sh
curl -i http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"destinationUrl": "ftp://example.com", "customAlias": "no"}'
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{
  "type": "urn:problem:url-shortener:validation-failed",
  "title": "The request is not valid",
  "status": 400,
  "detail": "The request has 2 invalid fields.",
  "errors": [
    {"field": "destinationUrl", "code": "url-unsupported-scheme", "message": "Enter a URL starting with http:// or https://"},
    {"field": "customAlias", "code": "alias-invalid-length", "message": "Alias must be between 3 and 64 characters"}
  ]
}
```

## 📮 Postman

[Postman](https://www.postman.com/) can build a ready-made collection from the
API's OpenAPI contract.

Download the contract that the running API service serves. Run this from your
host machine (change the port number if you're using a different one to serve
the API service).

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

To correlate a request with the service's logs (`make logs`), send your own
`X-Request-Id` header.
