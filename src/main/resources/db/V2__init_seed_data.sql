-- init data
INSERT INTO tracking_id_generator(origin_code, next_value, block_size)
SELECT 'DF', 1, 10000
WHERE NOT EXISTS (
  SELECT 1 FROM tracking_id_generator WHERE origin_code = 'DF'
);
INSERT INTO tracking_id_generator(origin_code, next_value, block_size)
SELECT 'MY', 1, 10000
WHERE NOT EXISTS (
  SELECT 1 FROM tracking_id_generator WHERE origin_code = 'MY'
);
