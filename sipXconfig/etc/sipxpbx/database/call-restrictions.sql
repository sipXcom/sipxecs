create table branch_auth_code
(
  auth_code_id integer not null,
  branch_id integer not null,
  constraint branch_auth_code_pkey primary key (auth_code_id, branch_id),
  constraint branch_auth_code_fk1 foreign key (auth_code_id)
      references auth_code (auth_code_id) match simple
      on update no action on delete no action,
  constraint branch_auth_code_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);

create table branch_auto_attendant
(
  auto_attendant_id integer not null,
  branch_id integer not null,
  constraint branch_auto_attendant_pkey primary key (auto_attendant_id, branch_id),
  constraint branch_auto_attendant_fk1 foreign key (auto_attendant_id)
      references auto_attendant (auto_attendant_id) match simple
      on update no action on delete no action,
  constraint branch_auto_attendant_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);

create table branch_aa_dialing_rule
(
  attendant_dialing_rule_id integer not null,
  branch_id integer not null,
  constraint branch_aa_dialing_rule_pkey primary key (attendant_dialing_rule_id, branch_id),
  constraint branch_aa_dialing_rule_fk1 foreign key (attendant_dialing_rule_id)
      references attendant_dialing_rule (attendant_dialing_rule_id) match simple
      on update no action on delete no action,
  constraint branch_aa_dialing_rule_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);

create table branch_park_orbit
(
  park_orbit_id integer not null,
  branch_id integer not null,
  constraint branch_park_orbit_pkey primary key (park_orbit_id, branch_id),
  constraint branch_park_orbit_fk1 foreign key (park_orbit_id)
      references park_orbit (park_orbit_id) match simple
      on update no action on delete no action,
  constraint branch_park_orbit_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);

create table branch_call_group
(
  call_group_id integer not null,
  branch_id integer not null,
  constraint branch_call_group_pkey primary key (call_group_id, branch_id),
  constraint branch_call_group_fk1 foreign key (call_group_id)
      references call_group (call_group_id) match simple
      on update no action on delete no action,
  constraint branch_call_group_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);

create table branch_paging_group
(
  paging_group_id integer not null,
  branch_id integer not null,
  constraint branch_paging_group_pkey primary key (paging_group_id, branch_id),
  constraint branch_paging_group_fk1 foreign key (paging_group_id)
      references paging_group (paging_group_id) match simple
      on update no action on delete no action,
  constraint branch_paging_group_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);

create table branch_call_queue
(
  freeswitch_ext_id integer not null,
  branch_id integer not null,
  constraint branch_call_queue_pkey primary key (freeswitch_ext_id, branch_id),
  constraint branch_call_queue_fk1 foreign key (freeswitch_ext_id)
      references freeswitch_extension (freeswitch_ext_id) match simple
      on update no action on delete no action,
  constraint branch_call_queue_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);

create table branch_conference
(
  meetme_conference_id integer not null,
  branch_id integer not null,
  constraint branch_conference_pkey primary key (meetme_conference_id, branch_id),
  constraint branch_conference_fk1 foreign key (meetme_conference_id)
      references meetme_conference (meetme_conference_id) match simple
      on update no action on delete no action,
  constraint branch_conference_fk2 foreign key (branch_id)
      references branch (branch_id) match simple
      on update no action on delete no action
);