CREATE TABLE IF NOT EXISTS "metadata"
(
    "id"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "group" TEXT    NOT NULL,
    "key"   TEXT    NOT NULL,
    "value" TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS "model_info"
(
    "id"              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "asset_id"        TEXT    NOT NULL,
    "store_path"      TEXT    NOT NULL,
    "type"            TEXT    NOT NULL,
    "style"           TEXT,
    "name"            TEXT    NOT NULL,
    "appellation"     TEXT,
    "skin_group_id"   TEXT    NOT NULL,
    "skin_group_name" TEXT    NOT NULL,
    "md5"             TEXT    NOT NULL,
    "exist"           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS "model_assets"
(
    "id"       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_id" INTEGER NOT NULL,
    "type"     TEXT    NOT NULL,
    "filename" TEXT    NOT NULL,
    CONSTRAINT "model_assets_fk_id" FOREIGN KEY ("model_id") REFERENCES "model_info" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS "model_tags"
(
    "id"       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "model_id" INTEGER NOT NULL,
    "tag"      TEXT    NOT NULL,
    CONSTRAINT "model_tags_fk_id" FOREIGN KEY ("model_id") REFERENCES "model_info" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS "model_info_asset_id_index" ON "model_info" ("asset_id");
CREATE INDEX IF NOT EXISTS "model_assets_model_id_index" ON "model_assets" ("model_id");
CREATE INDEX IF NOT EXISTS "model_tags_model_id_index" ON "model_tags" ("model_id");
