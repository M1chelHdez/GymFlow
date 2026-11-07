CREATE TABLE memberships (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  gym_id UUID NOT NULL REFERENCES gyms(id),
  member_id UUID NOT NULL REFERENCES members(id),
  plan_id UUID NOT NULL REFERENCES membership_plans(id),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','EXPIRED','CANCELLED')),
  agreed_price NUMERIC(12,2) NOT NULL CHECK (agreed_price >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_membership_dates CHECK (end_date > start_date)
);
CREATE INDEX idx_membership_gym_member ON memberships(gym_id, member_id);
CREATE INDEX idx_membership_gym_end_date ON memberships(gym_id, end_date);
