CREATE TABLE short_url (
    alias       VARCHAR(64)   NOT NULL,
    full_url    VARCHAR(2048) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT short_url_pkey PRIMARY KEY (alias)
);

CREATE INDEX short_url_created_at_idx ON short_url (created_at DESC, alias);
