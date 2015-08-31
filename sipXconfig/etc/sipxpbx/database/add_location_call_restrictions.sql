create table branch_route_domain (
    branch_id int4 not null,
    domain varchar(255) not null,
    index int4 not null,
    primary key (branch_id, index)
);

create table branch_route_subnet (
    branch_id int4 not null,
    subnet varchar(255) not null,
    index int4 not null,
    primary key (branch_id, index)
);

alter table branch_route_domain 
    add constraint fk_branch_route_domain 
    foreign key (branch_id) 
    references branch;

alter table branch_route_subnet 
    add constraint fk_branch_route_subnet 
    foreign key (branch_id) 
    references branch;