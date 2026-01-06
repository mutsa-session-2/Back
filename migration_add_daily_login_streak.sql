-- Add columns needed for daily login reward streak tracking
-- Safe to run multiple times

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS last_daily_login_reward_date DATE;

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS daily_login_streak INT;

-- Backfill: if column existed but had NULLs, normalize to 0
UPDATE user_profiles
SET daily_login_streak = 0
WHERE daily_login_streak IS NULL;

-- Ensure default and constraint after backfill
ALTER TABLE user_profiles
    ALTER COLUMN daily_login_streak SET DEFAULT 0;

ALTER TABLE user_profiles
    ALTER COLUMN daily_login_streak SET NOT NULL;
