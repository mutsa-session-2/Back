-- Supabase(PostgreSQL)용: teams 테이블에 프로젝트 기간 컬럼 추가
-- Supabase Dashboard → SQL Editor에서 실행하세요

-- 1) 컬럼 추가 (기존 데이터가 있어도 안전)
ALTER TABLE IF EXISTS teams
    ADD COLUMN IF NOT EXISTS start_date DATE,
    ADD COLUMN IF NOT EXISTS end_date DATE;

-- 2) (선택) 기존 데이터 백필(값 채우기)
-- 정책/요구사항이 정해지기 전이면 절대 임의의 날짜로 채우지 않는 게 안전합니다.
-- 아래 UPDATE는 예시이므로, 필요 시 팀 합의된 기본값으로 수정 후 사용하세요.
--
-- UPDATE teams
-- SET start_date = CURRENT_DATE,
--     end_date = CURRENT_DATE
-- WHERE start_date IS NULL OR end_date IS NULL;

-- 3) 널이 남아있지 않을 때만 NOT NULL 제약 적용 (안전하게 조건부 적용)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'teams'
    ) THEN
        RAISE NOTICE 'teams 테이블이 없어 NOT NULL 적용을 건너뜁니다.';
        RETURN;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'teams' AND column_name = 'start_date'
    ) OR NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'teams' AND column_name = 'end_date'
    ) THEN
        RAISE NOTICE 'teams.start_date 또는 teams.end_date 컬럼이 없어 NOT NULL 적용을 건너뜁니다.';
        RETURN;
    END IF;

    IF EXISTS (SELECT 1 FROM teams WHERE start_date IS NULL OR end_date IS NULL) THEN
        RAISE NOTICE 'teams에 start_date/end_date NULL 데이터가 있어 NOT NULL 적용을 건너뜁니다. 먼저 백필하세요.';
        RETURN;
    END IF;

    ALTER TABLE teams
        ALTER COLUMN start_date SET NOT NULL,
        ALTER COLUMN end_date SET NOT NULL;
    RAISE NOTICE 'teams.start_date/end_date NOT NULL 제약이 적용되었습니다.';
END $$;
