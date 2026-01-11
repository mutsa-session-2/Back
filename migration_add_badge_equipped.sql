-- Add equipped state to user_badges (for badge equip/unequip APIs)
-- Target DB: Postgres (Supabase)

ALTER TABLE IF EXISTS user_badges
ADD COLUMN IF NOT EXISTS equipped boolean NOT NULL DEFAULT false;

-- Ensure at most one equipped badge per user (optional but aligns with "현재 장착된 뱃지")
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_badges_one_equipped_per_user
ON user_badges (user_id)
WHERE equipped = true;
