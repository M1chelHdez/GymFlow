CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE gyms (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(120) NOT NULL,
  timezone VARCHAR(60) NOT NULL DEFAULT 'America/Mexico_City',
  currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED','INACTIVE')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  member_number VARCHAR(30) NOT NULL,
  full_name VARCHAR(140) NOT NULL,
  email VARCHAR(180),
  phone VARCHAR(30),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_member_gym_number UNIQUE (gym_id, member_number)
);
CREATE INDEX idx_member_gym_name ON members(gym_id, full_name);

CREATE TABLE membership_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  duration_value INTEGER NOT NULL CHECK (duration_value > 0),
  duration_unit VARCHAR(10) NOT NULL CHECK (duration_unit IN ('DAY','WEEK','MONTH','YEAR')),
  price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_plan_gym_name UNIQUE (gym_id, name)
);

CREATE TABLE product_categories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  name VARCHAR(80) NOT NULL,
  description VARCHAR(300),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uk_category_gym_name UNIQUE (gym_id, name)
);

CREATE TABLE suppliers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180),
  phone VARCHAR(30),
  notes VARCHAR(300),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uk_supplier_gym_name UNIQUE (gym_id, name)
);

CREATE TABLE products (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  category_id UUID REFERENCES product_categories(id),
  sku VARCHAR(40) NOT NULL,
  name VARCHAR(140) NOT NULL,
  description VARCHAR(500),
  cost_price NUMERIC(12,2) NOT NULL CHECK (cost_price >= 0),
  sale_price NUMERIC(12,2) NOT NULL CHECK (sale_price >= 0),
  minimum_stock INTEGER NOT NULL DEFAULT 0 CHECK (minimum_stock >= 0),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_product_gym_sku UNIQUE (gym_id, sku)
);
CREATE INDEX idx_product_gym_name ON products(gym_id, name);

CREATE TABLE stock_balances (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  product_id UUID NOT NULL REFERENCES products(id),
  quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_stock_gym_product UNIQUE (gym_id, product_id)
);

CREATE TABLE stock_movements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  product_id UUID NOT NULL REFERENCES products(id),
  type VARCHAR(20) NOT NULL CHECK (type IN ('INITIAL','IN','OUT','ADJUSTMENT')),
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  reason VARCHAR(250),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_stock_movement_gym_created ON stock_movements(gym_id, created_at DESC);
