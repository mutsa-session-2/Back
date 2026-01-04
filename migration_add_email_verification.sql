-- Email verification columns for users table (Supabase/PostgreSQL)
-- Safe to re-run.

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS email_verified boolean;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS email_verified_at timestamptz;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS email_verification_token_hash varchar(64);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS email_verification_token_expires_at timestamptz;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS email_verification_sent_at timestamptz;

-- Optional: index for token lookup
CREATE INDEX IF NOT EXISTS idx_users_email_verification_token_hash
  ON users (email_verification_token_hash);
