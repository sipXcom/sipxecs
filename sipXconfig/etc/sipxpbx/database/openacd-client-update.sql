ALTER table openacd_client ADD COLUMN archive_recordings CHARACTER VARYING(255);
ALTER table openacd_client ADD COLUMN retain_recordings_in_archive INTEGER NOT NULL DEFAULT 180;
