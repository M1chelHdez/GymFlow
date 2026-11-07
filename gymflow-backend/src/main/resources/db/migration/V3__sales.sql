CREATE TABLE sales (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  member_id UUID REFERENCES members(id),
  status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED','CANCELLED','REFUNDED')),
  payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CASH','CARD','TRANSFER','OTHER')),
  payment_status VARCHAR(20) NOT NULL DEFAULT 'RECORDED' CHECK (payment_status IN ('RECORDED','PENDING','REFUNDED')),
  subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0),
  discount_total NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (discount_total >= 0),
  total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_sales_gym_created ON sales(gym_id, created_at DESC);
CREATE TABLE sale_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sale_id UUID NOT NULL REFERENCES sales(id),
  product_id UUID NOT NULL REFERENCES products(id),
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
  unit_cost NUMERIC(12,2) NOT NULL CHECK (unit_cost >= 0),
  line_total NUMERIC(12,2) NOT NULL CHECK (line_total >= 0)
);
CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);
