alter table ldap_connection add column value_storage_id int;

alter table ldap_connection 
  add constraint ldap_connection_value_storage_id
    foreign key (value_storage_id) references value_storage;