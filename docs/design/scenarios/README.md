# Scenarios

This section covers the key end-to-end flows, traced through every container
they touch.

This view is for anyone who wants to see how a request flows through all the
components and architectural views.

Each scenario is mapped to one or more behavioral requirements by their unique
identifiers.

| Scenario                                                                            | Requirements    |
| ----------------------------------------------------------------------------------- | --------------- |
| [Shorten a URL with a custom alias, in the browser](./shorten-custom-alias.md)      | B2, B9          |
| [Submit an alias that's already taken](./alias-taken.md)                            | B4, B11         |
| [Shorten a URL with a random alias, via the API service](./shorten-random-alias.md) | B1              |
| [Follow a short URL](./follow-short-url.md)                                         | B5              |
| [Delete a short URL, in the browser](./delete-short-url.md)                         | B6, B10         |
| [The API service is unavailable](./api-unavailable.md)                              | B11             |
