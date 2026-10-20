# Configuration

Each component reads its settings from environment variables, once, at
startup, into a typed and validated object. It exits if any setting is missing
or invalid. The variables and their defaults are listed in `.env.example`.

Images contain no environment-specific configuration and no secrets.

## The two base URLs

The following two settings are easily confused, but are distinct.

| Setting           | Read by     | Meaning                                          | Local                   | Production                 |
| ----------------- | ----------- | ------------------------------------------------ | ----------------------- | -------------------------- |
| `PUBLIC_BASE_URL` | API service | Prefix of every `shortUrl`, as visitors see it.  | `http://localhost:8080` | `https://<short domain>`   |
| `API_BASE_URL`    | Web client  | Where the web client sends its calls to the API. | `http://api:8080`       | `http://api.internal:8080` |
