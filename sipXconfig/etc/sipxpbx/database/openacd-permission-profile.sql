create table openacd_permission_profile (
    openacd_permission_profile_id integer not null,
    name character varying(255) not null unique,
    monitor boolean NOT NULL,
    barge boolean NOT NULL,
    agent_manager boolean NOT NULL,
    queue_manager boolean NOT NULL,
    primary key (openacd_permission_profile_id)
);
create sequence openacd_permission_profile_seq;

insert into openacd_permission_profile (openacd_permission_profile_id, name, monitor, barge, agent_manager, queue_manager)
    values (nextval('openacd_permission_profile_seq'), 'supervisor', true, true, true, true);
insert into openacd_permission_profile (openacd_permission_profile_id, name, monitor, barge, agent_manager, queue_manager)
    values (nextval('openacd_permission_profile_seq'), 'agent', false, false, false, false);

create table openacd_permission_agent_group (
  openacd_agent_group_id integer not null,
  openacd_permission_profile_id integer not null,
  constraint openacd_permission_agent_group_pkey primary key (openacd_agent_group_id, openacd_permission_profile_id),
  constraint permission_agent_group_fk1 foreign key (openacd_agent_group_id)
      references openacd_agent_group (openacd_agent_group_id) match simple
      on update no action on delete no action,
  constraint permission_agent_group_fk2 foreign key (openacd_permission_profile_id)
      references openacd_permission_profile (openacd_permission_profile_id) match simple
      on update no action on delete no action
);

create table openacd_permission_queue (
  openacd_queue_id integer not null,
  openacd_permission_profile_id integer not null,
  constraint openacd_permission_queue_pkey primary key (openacd_queue_id, openacd_permission_profile_id),
  constraint permission_queue_fk1 foreign key (openacd_queue_id)
      references openacd_queue (openacd_queue_id) match simple
      on update no action on delete no action,
  constraint permission_queue_fk2 foreign key (openacd_permission_profile_id)
      references openacd_permission_profile (openacd_permission_profile_id) match simple
      on update no action on delete no action
);

-- add permission profile column to AGENT table
alter table openacd_agent add column openacd_permission_profile_id integer NOT NULL;
alter table openacd_agent add constraint fk_openacd_permission_profile foreign key (openacd_permission_profile_id)
      references openacd_permission_profile (openacd_permission_profile_id) match simple
      on update no action on delete no action;
