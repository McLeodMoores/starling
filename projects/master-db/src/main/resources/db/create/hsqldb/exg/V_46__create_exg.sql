
-- create-db-refdata.sql

-- Holiday Master design has one document
--  holiday and associated dates
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE hol_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO hol_schema_version (version_key, version_value) VALUES ('schema_patch', '46');
  
CREATE SEQUENCE IF NOT EXISTS hol_holiday_seq AS bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE hol_holiday (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    name varchar(255) NOT NULL,
    provider_scheme varchar(255),
    provider_value varchar(255),
    hol_type varchar(255) NOT NULL,
    region_scheme varchar(255),
    region_value varchar(255),
    exchange_scheme varchar(255),
    exchange_value varchar(255),
    custom_scheme varchar(255),
    custom_value varchar(255),
    currency_iso varchar(255),
    PRIMARY KEY (id),
    CONSTRAINT hol_chk_holiday_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT hol_chk_holiday_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_hol_holiday_oid ON hol_holiday(oid);
CREATE INDEX ix_hol_holiday_ver_from_instant ON hol_holiday(ver_from_instant);
CREATE INDEX ix_hol_holiday_ver_to_instant ON hol_holiday(ver_to_instant);
CREATE INDEX ix_hol_holiday_corr_from_instant ON hol_holiday(corr_from_instant);
CREATE INDEX ix_hol_holiday_corr_to_instant ON hol_holiday(corr_to_instant);
CREATE INDEX ix_hol_holiday_name ON hol_holiday(name);
-- CREATE INDEX ix_hol_holiday_nameu ON hol_holiday(UPPER(name));
CREATE INDEX ix_hol_holiday_provider_scheme ON hol_holiday(provider_scheme);
CREATE INDEX ix_hol_holiday_provider_value ON hol_holiday(provider_value);
CREATE INDEX ix_hol_holiday_holiday_type ON hol_holiday(hol_type);
CREATE INDEX ix_hol_holiday_region_scheme ON hol_holiday(region_scheme);
CREATE INDEX ix_hol_holiday_region_value ON hol_holiday(region_value);
CREATE INDEX ix_hol_holiday_exchange_scheme ON hol_holiday(exchange_scheme);
CREATE INDEX ix_hol_holiday_exchange_value ON hol_holiday(exchange_value);
CREATE INDEX ix_hol_holiday_custom_scheme ON hol_holiday(custom_scheme);
CREATE INDEX ix_hol_holiday_custom_value ON hol_holiday(custom_value);
CREATE INDEX ix_hol_holiday_currency_iso ON hol_holiday(currency_iso);

CREATE TABLE hol_date (
    holiday_id bigint NOT NULL,
    hol_date date NOT NULL,
    CONSTRAINT hol_fk_date2hol FOREIGN KEY (holiday_id) REFERENCES hol_holiday (id)
);
CREATE INDEX ix_hol_date_holiday_id ON hol_date(holiday_id);


-- Exchange Master design has one document
--  exchange and associated identifiers
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE exg_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO exg_schema_version (version_key, version_value) VALUES ('schema_patch', '46');

CREATE SEQUENCE IF NOT EXISTS exg_exchange_seq AS bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS exg_idkey_seq as bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE exg_exchange (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    name varchar(255) NOT NULL,
    time_zone varchar(255),
    detail blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT exg_chk_exchange_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT exg_chk_exchange_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_exg_exchange_oid ON exg_exchange(oid);
CREATE INDEX ix_exg_exchange_ver_from_instant ON exg_exchange(ver_from_instant);
CREATE INDEX ix_exg_exchange_ver_to_instant ON exg_exchange(ver_to_instant);
CREATE INDEX ix_exg_exchange_corr_from_instant ON exg_exchange(corr_from_instant);
CREATE INDEX ix_exg_exchange_corr_to_instant ON exg_exchange(corr_to_instant);
CREATE INDEX ix_exg_exchange_name ON exg_exchange(name);
-- CREATE INDEX ix_exg_exchange_nameu ON exg_exchange(UPPER(name));

CREATE TABLE exg_idkey (
    id bigint NOT NULL,
    key_scheme varchar(255) NOT NULL,
    key_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT exg_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE exg_exchange2idkey (
    exchange_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    PRIMARY KEY (exchange_id, idkey_id),
    CONSTRAINT exg_fk_exgidkey2exg FOREIGN KEY (exchange_id) REFERENCES exg_exchange (id),
    CONSTRAINT exg_fk_exgidkey2idkey FOREIGN KEY (idkey_id) REFERENCES exg_idkey (id)
);
CREATE INDEX ix_exg_exg2idkey_idkey ON exg_exchange2idkey(idkey_id);
-- exg_exchange2idkey is fully dependent of exg_exchange
