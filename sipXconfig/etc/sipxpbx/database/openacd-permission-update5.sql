ALTER table openacd_permission_profile ADD COLUMN allow_outbound boolean NOT NULL DEFAULT FALSE;

update openacd_permission_profile set allow_outbound='true' where openacd_permission_profile_id in
(select openacd_permission_profile_id from openacd_permission_widget where widget='OutboundCall')
