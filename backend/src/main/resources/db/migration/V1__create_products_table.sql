CREATE TABLE products (
    id          UUID    NOT NULL,
    name        TEXT    NOT NULL,
    description TEXT    NOT NULL,
    price_cents BIGINT  NOT NULL,
    currency    CHAR(3) NOT NULL,
    image_url   TEXT    NOT NULL,

    CONSTRAINT products_pkey PRIMARY KEY (id)
);
