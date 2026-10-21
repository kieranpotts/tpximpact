# Configuration

Each component reads its settings from environment variables, once, at
startup, into a typed and validated object. It has no defaults. It exits with
status 1, naming every setting that's missing or invalid.

Images contain no environment-specific configuration and no secrets.
`compose.yaml` sets a working value for every variable. The few that are
worth overriding locally are listed in `.env.example`.

## API service

| Setting             | Meaning                                                             | Local                                     |
| ------------------- | ------------------------------------------------------------------- | ----------------------------------------- |
| `PUBLIC_BASE_URL`   | Prefix of every `shortUrl`. An `http` or `https` URL, with no path. | `http://localhost:8080`                   |
| `DATABASE_URL`      | The database, as a PostgreSQL JDBC URL.                             | `jdbc:postgresql://db:5432/url_shortener` |
| `DATABASE_USER`     | The database user. It also runs the migrations.                     | `url_shortener`                           |
| `DATABASE_PASSWORD` | The database user's password. A secret, everywhere but locally.     | `url_shortener`                           |

## Gotchas

The following two settings are easily confused, but are distinct.

| Setting           | Read by     | Meaning                                          | Local                   | Production                 |
| ----------------- | ----------- | ------------------------------------------------ | ----------------------- | -------------------------- |
| `PUBLIC_BASE_URL` | API service | Prefix of every `shortUrl`, as visitors see it.  | `http://localhost:8080` | `https://<short domain>`   |
| `API_BASE_URL`    | Web client  | Where the web client sends its calls to the API. | `http://api:8080`       | `http://api.internal:8080` |
