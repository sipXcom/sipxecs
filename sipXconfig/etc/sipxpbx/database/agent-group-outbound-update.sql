alter table openacd_agent_group add column no_customer_outbound boolean DEFAULT TRUE;

alter table openacd_agent_group add column outbound_client_id integer;

alter table openacd_agent_group add constraint fk_outbound_client_id foreign key (outbound_client_id)
      references openacd_client (openacd_client_id) match simple
      on update no action on delete no action;