-- 기존 Character 레코드에 equipped_items 기본값 설정
-- 서버 재시작 후 MySQL에서 실행하세요

-- 0. 데이터베이스 선택 (필수!)
USE floorida;

-- 1. Safe mode 비활성화 (임시)
SET SQL_SAFE_UPDATES = 0;

-- 2. equipped_items가 NULL인 레코드에 빈 JSON 설정
UPDATE characters 
SET equipped_items = '{}' 
WHERE equipped_items IS NULL;

-- 3. Safe mode 다시 활성화
SET SQL_SAFE_UPDATES = 1;

-- 2. 확인
SELECT character_id, user_id, image_url, equipped_items 
FROM characters;
