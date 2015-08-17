create table openacd_agent_pstn (
    openacd_agent_id integer not null,
    pstn_number varchar(255) NOT NULL unique,
    index integer NOT NULL,
    constraint pstn_number_pkey primary key (openacd_agent_id, index),
    constraint fk_openacd_agent foreign key (openacd_agent_id)
      references openacd_agent (openacd_agent_id) match simple
      on update no action on delete cascade
);
