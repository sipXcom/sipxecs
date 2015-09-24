delete from setting_value where path='configserver-config/systemAudit';

DROP SEQUENCE IF EXISTS config_change_seq;
DROP SEQUENCE IF EXISTS config_change_value_seq;
DROP TABLE IF EXISTS config_change_value;
DROP TABLE IF EXISTS config_change;

DROP FUNCTION IF EXISTS system_audit_cleanup();
