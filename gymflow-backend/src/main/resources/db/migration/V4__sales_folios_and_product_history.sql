-- Human-friendly sale folios; existing sales receive a stable folio without changing their UUID.
CREATE SEQUENCE IF NOT EXISTS sales_folio_seq START WITH 1;
ALTER TABLE sales ADD COLUMN IF NOT EXISTS folio VARCHAR(20);
WITH numbered AS (
  SELECT id, gym_id, row_number() OVER (PARTITION BY gym_id ORDER BY created_at, id) AS n
  FROM sales WHERE folio IS NULL
)
UPDATE sales s SET folio = 'VTA-' || lpad(numbered.n::text, 6, '0')
FROM numbered WHERE s.id = numbered.id;
ALTER TABLE sales ALTER COLUMN folio SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sales_gym_folio ON sales(gym_id, folio);
UPDATE stock_movements sm SET reason = 'Venta de producto · ' || s.folio FROM sales s WHERE sm.reason = 'Venta ' || s.id AND sm.gym_id = s.gym_id;
SELECT setval('sales_folio_seq', COALESCE(MAX(NULLIF(regexp_replace(folio, '[^0-9]', '', 'g'), '')::BIGINT), 1), COALESCE(MAX(NULLIF(regexp_replace(folio, '[^0-9]', '', 'g'), '')::BIGINT), 0) > 0) FROM sales;

CREATE SEQUENCE IF NOT EXISTS product_sku_seq START WITH 1;

CREATE TABLE IF NOT EXISTS product_change_history (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  product_id UUID NOT NULL REFERENCES products(id),
  field_name VARCHAR(60) NOT NULL,
  old_value TEXT,
  new_value TEXT,
  changed_by VARCHAR(120) NOT NULL DEFAULT 'Administrador demo',
  changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_product_history_product_date ON product_change_history(gym_id, product_id, changed_at DESC);
