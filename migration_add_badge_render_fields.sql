-- Add rendering fields to badges (for badge equip overlay)
-- Target DB: Postgres (Supabase)

ALTER TABLE IF EXISTS badges
ADD COLUMN IF NOT EXISTS offset_x integer,
ADD COLUMN IF NOT EXISTS offset_y integer,
ADD COLUMN IF NOT EXISTS width integer,
ADD COLUMN IF NOT EXISTS height integer;
