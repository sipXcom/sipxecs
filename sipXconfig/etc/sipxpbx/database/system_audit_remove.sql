insert into feature_local (feature_id, location_id) select 'systemaudit',l.location_id from
location l, setting_value s where l.name='Primary' and s.path='configserver-config/systemAudit';

insert into feature_local (feature_id, location_id) select 'elasticsearch',l.location_id from
location l, setting_value s where l.name='Primary' and s.path='configserver-config/systemAudit';

delete from setting_value where path='configserver-config/systemAudit';

DROP SEQUENCE IF EXISTS config_change_seq;
DROP SEQUENCE IF EXISTS config_change_value_seq;
DROP TABLE IF EXISTS config_change_value;
DROP TABLE IF EXISTS config_change;

DROP FUNCTION IF EXISTS system_audit_cleanup();
