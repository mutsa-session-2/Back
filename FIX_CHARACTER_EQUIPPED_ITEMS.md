# Character equipped_items 필드 추가 해결 방법

## 문제 상황
- 기존 계정으로 `/api/characters/me` 호출 시 500 에러 발생
- 원인: Character 테이블에 `equipped_items` 필드가 없어서 NULL 상태

## 해결 방법

### 1️⃣ 서버 재시작 (이미 완료)
서버를 재시작하면 Hibernate가 자동으로 `equipped_items` 컬럼을 추가합니다.

### 2️⃣ 기존 데이터 수정 (필수!)

MySQL Workbench 또는 터미널에서 다음 SQL 실행:

```sql
-- floorida 데이터베이스 선택
USE floorida;

-- 기존 레코드의 equipped_items를 빈 JSON으로 설정
UPDATE characters 
SET equipped_items = '{}' 
WHERE equipped_items IS NULL;

-- 확인
SELECT character_id, user_id, image_url, equipped_items 
FROM characters;
```

### 3️⃣ 다시 API 호출

```bash
GET http://localhost:8080/api/characters/me
Authorization: Bearer {토큰}
```

**예상 응답:**
```json
{
  "characterId": 1,
  "imageUrl": "https://floorida-bucket.s3.us-east-1.amazonaws.com/캐릭터.png"
}
```

## 변경 사항

### Character 엔티티
```java
// 추가된 필드
@Column(name = "equipped_items", columnDefinition = "JSON", nullable = true)
private String equippedItems;
```

### 새 계정 가입 시
- 자동으로 `equippedItems = "{}"` 설정됨
- 기존 계정은 수동 SQL 업데이트 필요

## 향후 아이템 장착 로직

```java
// 1. 아이템 장착 예시
String equippedJson = "{\"hat\": 5, \"face\": 12, \"accessory\": 8}";
character.setEquippedItems(equippedJson);

// 2. JSON 파싱
ObjectMapper mapper = new ObjectMapper();
Map<String, Integer> items = mapper.readValue(
    character.getEquippedItems(),
    new TypeReference<Map<String, Integer>>() {}
);

// 3. 특정 슬롯 조회
Integer hatId = items.get("hat");  // 5
```
