update park_orbit set location_id=(select location_id from location where primary_location=true) WHERE location_id IS NULL;
