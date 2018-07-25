CREATE TABLE "lots" (
  "id"          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "data"        VARCHAR NOT NULL,
  "auction_id"  UUID NOT NULL
);

ALTER TABLE "lots" ADD CONSTRAINT "auction_fk" FOREIGN KEY ("auction_id") REFERENCES "auctions" ("id") ON UPDATE NO ACTION ON DELETE CASCADE;
