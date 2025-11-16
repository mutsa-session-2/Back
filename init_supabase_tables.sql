-- Supabase PostgreSQL 테이블 초기화 스크립트
-- Supabase Dashboard → SQL Editor에서 실행하세요

-- 1. users 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. user_profiles 테이블
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY,
    planning_tendency VARCHAR(50),
    daily_study_hours VARCHAR(50),
    points INT NOT NULL DEFAULT 0,
    personal_level INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. schedules 테이블
CREATE TABLE IF NOT EXISTS schedules (
    schedule_id BIGSERIAL PRIMARY KEY,
    creator_user_id BIGINT NOT NULL,
    team_id BIGINT,
    title VARCHAR(255) NOT NULL,
    original_goal VARCHAR(1000),
    goal_summary VARCHAR(2000),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (creator_user_id) REFERENCES users(user_id)
);

-- 4. floors 테이블
CREATE TABLE IF NOT EXISTS floors (
    floor_id BIGSERIAL PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    creator_user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    scheduled_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id) ON DELETE CASCADE,
    FOREIGN KEY (creator_user_id) REFERENCES users(user_id)
);

-- 5. floor_statuses 테이블
CREATE TABLE IF NOT EXISTS floor_statuses (
    status_id BIGSERIAL PRIMARY KEY,
    floor_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (floor_id) REFERENCES floors(floor_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE(floor_id, user_id)
);

-- 6. characters 테이블
CREATE TABLE IF NOT EXISTS characters (
    character_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    equipped_items JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 인덱스 생성 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_schedules_creator ON schedules(creator_user_id);
CREATE INDEX IF NOT EXISTS idx_floors_schedule ON floors(schedule_id);
CREATE INDEX IF NOT EXISTS idx_floors_creator_date ON floors(creator_user_id, scheduled_date);
CREATE INDEX IF NOT EXISTS idx_floor_statuses_floor_user ON floor_statuses(floor_id, user_id);

-- 완료!
SELECT 'Tables created successfully!' as status;

