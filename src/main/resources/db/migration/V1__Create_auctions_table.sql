CREATE TABLE "auctions" (
  "id"       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "data"     VARCHAR NOT NULL
);
