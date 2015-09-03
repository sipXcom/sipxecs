-- ensure that existing users are converted appropriately
update openacd_agent set openacd_permission_profile_id = (SELECT openacd_permission_profile_id FROM openacd_permission_profile where name='agent') 
where security='AGENT';
update openacd_agent set openacd_permission_profile_id = 
(SELECT openacd_permission_profile_id FROM openacd_permission_profile where name='supervisor') where security in ('ADMIN','SUPERVISOR');

-- drop security column from openacd_agent
alter table openacd_agent drop column security;
