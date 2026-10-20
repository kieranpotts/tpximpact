# Overview

The URL shortener turns long URLs into short ones.

A short URL is the system's **public base URL** followed by an **alias**, for
example `http://localhost:8080/my-alias`. Following it redirects to the
**destination URL**.

The system has no concept of users or authentication/authorization at this
time. Also out-of-scope are:

- High availability, horizontal scaling, and multi-region deployment.
- Rate limiting and abuse prevention.
- Screening of destination URLs for malware or phishing.
- Filtering aliases, eg. for obscenity.
- Click analytics and tracking.
- Link expiry, and editing aliases after creation.
- Custom domains. The public base URL is a single configured value.
- Data retention, backup, and deletion policies.
- Any user interface beyond the HTTP API and web client.
