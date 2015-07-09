create table openacd_permission_widget
(
  openacd_permission_profile_id integer not null,
  widget character varying(255) not null,
  constraint openacd_permission_widget_pkey primary key (openacd_permission_profile_id, widget),
  constraint openacd_permission_widget_fk1 FOREIGN KEY (openacd_permission_profile_id)
      references openacd_permission_profile (openacd_permission_profile_id) match simple
      on update no action on delete cascade
);

insert into openacd_permission_widget (openacd_permission_profile_id, widget) 
    select p.openacd_permission_profile_id, 'AgentManager' from openacd_permission_profile as p where p.name='supervisor';
insert into openacd_permission_widget (openacd_permission_profile_id, widget) 
    select p.openacd_permission_profile_id, 'CallRecording' from openacd_permission_profile as p where p.name='supervisor';
insert into openacd_permission_widget (openacd_permission_profile_id, widget) 
    select p.openacd_permission_profile_id, 'MyStatistics' from openacd_permission_profile as p where p.name='supervisor';
insert into openacd_permission_widget (openacd_permission_profile_id, widget) 
    select p.openacd_permission_profile_id, 'OutboundCall' from openacd_permission_profile as p where p.name='supervisor';
insert into openacd_permission_widget (openacd_permission_profile_id, widget) 
    select p.openacd_permission_profile_id, 'QueueManager' from openacd_permission_profile as p where p.name='supervisor';

insert into openacd_permission_widget (openacd_permission_profile_id, widget) 
    select p.openacd_permission_profile_id, 'MyStatistics' from openacd_permission_profile as p where p.name='agent';

ALTER table openacd_permission_profile ADD COLUMN customize_desktop boolean NOT NULL DEFAULT FALSE;
ALTER table openacd_permission_profile ADD COLUMN use_advanced_login boolean NOT NULL DEFAULT FALSE;
ALTER table openacd_permission_profile ADD COLUMN transfer_to_agent boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN transfer_to_queue boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN transfer_to_number boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN conference_to_agent boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN conference_to_queue boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN conference_to_number boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN change_skills_on_tran_conf boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN control_agent_state boolean NOT NULL DEFAULT TRUE;
ALTER table openacd_permission_profile ADD COLUMN reports_tab boolean NOT NULL DEFAULT FALSE;
ALTER table openacd_permission_profile ADD COLUMN supervisor_tab boolean NOT NULL DEFAULT FALSE;

UPDATE openacd_permission_profile SET 
    customize_desktop = true,
    use_advanced_login = true,
    transfer_to_agent = true,
    transfer_to_queue = true,
    transfer_to_number = true,
    conference_to_agent = true,
    conference_to_queue = true,
    conference_to_number = true,
    change_skills_on_tran_conf = true,
    control_agent_state = true,
    reports_tab = true,
    supervisor_tab = true
    WHERE name='supervisor';

UPDATE openacd_permission_profile SET 
    customize_desktop = true,
    use_advanced_login = false,
    transfer_to_agent = true,
    transfer_to_queue = true,
    transfer_to_number = true,
    conference_to_agent = true,
    conference_to_queue = true,
    conference_to_number = true,
    change_skills_on_tran_conf = true,
    control_agent_state = true,
    reports_tab = false,
    supervisor_tab = false
    WHERE name='agent';
