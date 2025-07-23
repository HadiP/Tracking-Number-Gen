-- init data
INSERT INTO tracking_id_generator(instance_name, next_value, block_size)
SELECT 'default', 1, 10000
WHERE NOT EXISTS (
  SELECT 1 FROM tracking_id_generator WHERE instance_name = 'default'
);
INSERT INTO tracking_id_generator(instance_name, next_value, block_size)
SELECT 'MY', 1, 10000
WHERE NOT EXISTS (
  SELECT 1 FROM tracking_id_generator WHERE instance_name = 'MY'
);
