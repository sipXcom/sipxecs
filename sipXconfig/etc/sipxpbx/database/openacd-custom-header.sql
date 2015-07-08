create table openacd_custom_header (
    openacd_custom_header_id integer not null,
    name character varying(255) not null unique,
    active boolean default false,
    description character varying(255) not null,
    primary key (openacd_custom_header_id)
);
create sequence openacd_custom_header_seq;
