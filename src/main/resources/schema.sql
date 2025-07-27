-- init table
CREATE TABLE IF NOT EXISTS tracking_id_generator (
  origin_code VARCHAR(2) PRIMARY KEY,
  next_value BIGINT NOT NULL,
  block_size INT NOT NULL,
  last_reset_timestamp TIMESTAMP WITH TIME ZONE
);