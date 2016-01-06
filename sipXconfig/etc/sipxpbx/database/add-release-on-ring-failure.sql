ALTER TABLE openacd_agent_group ADD COLUMN release_on_ring_failure INTEGER;
UPDATE openacd_agent_group SET release_on_ring_failure = 3;
