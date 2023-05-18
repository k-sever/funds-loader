CREATE TABLE "funds" (
    "id" integer NOT NULL,
    "customer_id" integer NOT NULL,
    "time" timestamp NOT NULL,
    "amount" integer NOT NULL,
    "accepted" boolean NOT NULL,
    PRIMARY KEY ("id", "customer_id")
);