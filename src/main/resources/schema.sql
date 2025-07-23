-- init table
CREATE TABLE IF NOT EXISTS tracking_id_generator (
  instance_name VARCHAR PRIMARY KEY,
  next_value BIGINT NOT NULL,
  block_size INT NOT NULL
);