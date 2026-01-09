-- Relax constraints for badge rendering fields (allow nulls)
-- Target DB: Postgres (Supabase)

ALTER TABLE IF EXISTS badges
    ALTER COLUMN offset_x DROP NOT NULL,
    ALTER COLUMN offset_x DROP DEFAULT,
    ALTER COLUMN offset_y DROP NOT NULL,
    ALTER COLUMN offset_y DROP DEFAULT,
    ALTER COLUMN width DROP NOT NULL,
    ALTER COLUMN width DROP DEFAULT,
    ALTER COLUMN height DROP NOT NULL,
    ALTER COLUMN height DROP DEFAULT;
