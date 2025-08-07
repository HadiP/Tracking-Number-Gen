-- init table
CREATE TABLE IF NOT EXISTS tracking_id_generator (
  origin_code VARCHAR(2) PRIMARY KEY,
  next_value BIGINT NOT NULL,
  block_size INT NOT NULL,
  last_reset_timestamp TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS order_records (
    id UUID PRIMARY KEY,
    tracking_number VARCHAR(255) UNIQUE,
    origin_country_id CHAR(2) NOT NULL,
    destination_country_id CHAR(2) NOT NULL,
    weight DECIMAL(6,3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_slug VARCHAR(255) NOT NULL
);