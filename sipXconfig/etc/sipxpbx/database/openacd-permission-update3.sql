-- add permission profile column to AGENT GROUP table
alter table openacd_agent_group add column openacd_permission_profile_id integer;

-- all existing agents will have default "agent" permission profile
update openacd_agent_group set openacd_permission_profile_id = (SELECT openacd_permission_profile_id FROM openacd_permission_profile where name='agent');

alter table openacd_agent_group alter column openacd_permission_profile_id set NOT NULL;

alter table openacd_agent_group add constraint fk_openacd_permission_profile foreign key (openacd_permission_profile_id)
      references openacd_permission_profile (openacd_permission_profile_id) match simple
      on update no action on delete no action;
