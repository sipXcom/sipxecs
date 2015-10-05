alter table openacd_agent_group add column auto_logout boolean default false;
alter table openacd_agent_group add column release_duration character varying(255) default '0';
