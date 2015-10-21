alter table branch add column fallback_branch_id int4;
alter table branch add constraint fk_fallback_branch
  foreign key (fallback_branch_id)
  references branch(branch_id) match full
  on delete set null;