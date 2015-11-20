START TRANSACTION;
  ALTER TABLE hol_holiday ADD weekend_type VARCHAR(255) NULL;
  UPDATE exg_schema_version SET version_value='47' WHERE version_key='schema_patch';  
COMMIT;