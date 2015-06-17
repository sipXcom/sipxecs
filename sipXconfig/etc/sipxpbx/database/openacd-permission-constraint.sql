alter table openacd_permission_agent_group drop constraint permission_agent_group_fk1;
alter table openacd_permission_agent_group add constraint permission_agent_group_fk1 foreign key (openacd_agent_group_id)
      references openacd_agent_group (openacd_agent_group_id) match simple
      on update no action on delete cascade;
alter table openacd_permission_agent_group drop constraint permission_agent_group_fk2;
alter table openacd_permission_agent_group add constraint permission_agent_group_fk2 foreign key (openacd_permission_profile_id)
      references openacd_permission_profile (openacd_permission_profile_id) match simple
      on update no action on delete cascade;

alter table openacd_permission_queue drop constraint permission_queue_fk1;
alter table openacd_permission_queue add constraint permission_queue_fk1 foreign key (openacd_queue_id)
      references openacd_queue (openacd_queue_id) match simple
      on update no action on delete cascade;
alter table openacd_permission_queue drop constraint permission_queue_fk2;
alter table openacd_permission_queue add constraint permission_queue_fk2 foreign key (openacd_permission_profile_id)
      references openacd_permission_profile (openacd_permission_profile_id) match simple
      on update no action on delete cascade;
