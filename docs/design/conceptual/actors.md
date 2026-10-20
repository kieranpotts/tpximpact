# Actors

Three kinds of actor use the system.

- **API client** \
  Any program that calls the API directly to shorten, list, or delete URLs.
  The web client is an API client.

- **Link editor** \
  A person who shortens, lists, or deletes URLs using the web client.

- **Short URL visitor** \
  A person, or crawler, who follows a short URL. The API client handles the
  redirect, but the short URL visitor is not necessarily aware that this or any
  other part of the system exists.
