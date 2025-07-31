# 게시판 API 엔드포인트 문서

## 🚀 주요 API 엔드포인트

### 기본 CRUD 작업

#### 1. 게시글 생성
- **URL**: `POST /api/boards`
- **설명**: 새로운 게시글을 생성합니다.
- **Request Body**:
```json
{
  "title": "게시글 제목",
  "content": "게시글 내용",
  "author": "작성자명"
}
```
- **Response**: `201 Created`
```json
{
  "id": 1,
  "title": "게시글 제목",
  "content": "게시글 내용",
  "author": "작성자명",
  "createdAt": "2025-07-31T12:00:00",
  "updatedAt": "2025-07-31T12:00:00"
}
```

#### 2. 게시글 단건 조회
- **URL**: `GET /api/boards/{id}`
- **설명**: 특정 ID의 게시글을 조회합니다.
- **Response**: `200 OK`
```json
{
  "id": 1,
  "title": "게시글 제목",
  "content": "게시글 내용",
  "author": "작성자명",
  "createdAt": "2025-07-31T12:00:00",
  "updatedAt": "2025-07-31T12:00:00"
}
```

#### 3. 전체 게시글 조회 (페이징)
- **URL**: `GET /api/boards`
- **설명**: 페이징 처리된 전체 게시글을 최신순으로 조회합니다.
- **Parameters**:
  - `page` (optional): 페이지 번호 (기본값: 0)
  - `size` (optional): 페이지 크기 (기본값: 10)
- **Example**: `GET /api/boards?page=0&size=20`
- **Response**: `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "게시글 제목",
      "content": "게시글 내용",
      "author": "작성자명",
      "createdAt": "2025-07-31T12:00:00",
      "updatedAt": "2025-07-31T12:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 100,
  "totalPages": 10
}
```

#### 4. 전체 게시글 조회 (리스트)
- **URL**: `GET /api/boards/list`
- **설명**: 전체 게시글을 리스트 형태로 조회합니다 (페이징 없음).
- **Response**: `200 OK`
```json
[
  {
    "id": 1,
    "title": "게시글 제목",
    "content": "게시글 내용",
    "author": "작성자명",
    "createdAt": "2025-07-31T12:00:00",
    "updatedAt": "2025-07-31T12:00:00"
  }
]
```

#### 5. 게시글 수정
- **URL**: `PUT /api/boards/{id}`
- **설명**: 특정 ID의 게시글을 수정합니다.
- **Request Body**:
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용"
}
```
- **Response**: `200 OK`
```json
{
  "id": 1,
  "title": "수정된 제목",
  "content": "수정된 내용",
  "author": "작성자명",
  "createdAt": "2025-07-31T12:00:00",
  "updatedAt": "2025-07-31T12:30:00"
}
```

#### 6. 게시글 삭제
- **URL**: `DELETE /api/boards/{id}`
- **설명**: 특정 ID의 게시글을 삭제합니다.
- **Response**: `204 No Content`

---

### 검색 기능

#### 1. 제목으로 검색
- **URL**: `GET /api/boards/search/title`
- **설명**: 제목에 특정 문자열이 포함된 게시글을 검색합니다.
- **Parameters**:
  - `title`: 검색할 제목 키워드
- **Example**: `GET /api/boards/search/title?title=Spring`
- **Response**: `200 OK` (배열 형태)

#### 2. 작성자로 검색
- **URL**: `GET /api/boards/search/author`
- **설명**: 특정 작성자의 게시글을 검색합니다.
- **Parameters**:
  - `author`: 작성자명
- **Example**: `GET /api/boards/search/author?author=홍길동`
- **Response**: `200 OK` (배열 형태)

#### 3. 키워드 통합 검색
- **URL**: `GET /api/boards/search`
- **설명**: 제목 또는 내용에 키워드가 포함된 게시글을 검색합니다.
- **Parameters**:
  - `keyword`: 검색 키워드
- **Example**: `GET /api/boards/search?keyword=Spring Boot`
- **Response**: `200 OK` (배열 형태)

#### 4. 최신 게시글 조회
- **URL**: `GET /api/boards/latest`
- **설명**: 최신 게시글 10개를 조회합니다.
- **Response**: `200 OK` (배열 형태)

---

### 성능 테스트용 API

#### 대량 데이터 생성
- **URL**: `POST /api/boards/bulk`
- **설명**: 성능 테스트를 위한 대량의 게시글을 생성합니다.
- **Parameters**:
  - `count` (optional): 생성할 게시글 수 (기본값: 1000)
- **Example**: `POST /api/boards/bulk?count=5000`
- **Response**: `200 OK`
```json
"5000개의 게시글이 생성되었습니다."
```

---

## 🗄️ 데이터베이스 설정

### MySQL 연결 정보
- **스키마**: `boards`
- **사용자**: `root`
- **비밀번호**: `1234`
- **포트**: `3306`
- **URL**: `jdbc:mysql://localhost:3306/boards`

### 스키마 생성 SQL
```sql
CREATE DATABASE boards CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 🏷️ 응답 상태 코드

| 상태 코드 | 설명 |
|----------|------|
| 200 OK | 요청 성공 |
| 201 Created | 리소스 생성 성공 |
| 204 No Content | 삭제 성공 |
| 400 Bad Request | 잘못된 요청 |
| 404 Not Found | 리소스를 찾을 수 없음 |
| 500 Internal Server Error | 서버 내부 오류 |

---

## 📝 사용 예시

### cURL을 사용한 테스트

#### 게시글 생성
```bash
curl -X POST http://localhost:8080/api/boards \
  -H "Content-Type: application/json" \
  -d '{
    "title": "첫 번째 게시글",
    "content": "안녕하세요. 첫 번째 게시글입니다.",
    "author": "관리자"
  }'
```

#### 게시글 조회
```bash
curl -X GET http://localhost:8080/api/boards/1
```

#### 페이징 조회
```bash
curl -X GET "http://localhost:8080/api/boards?page=0&size=5"
```

#### 검색
```bash
curl -X GET "http://localhost:8080/api/boards/search?keyword=Spring"
```

#### 대량 데이터 생성 (성능 테스트용)
```bash
curl -X POST "http://localhost:8080/api/boards/bulk?count=10000"
```

---

## ⚡ 성능 테스트 시나리오

1. **대량 데이터 생성**: `/api/boards/bulk` 엔드포인트로 테스트 데이터 생성
2. **조회 성능 측정**: 
   - 전체 조회: `/api/boards/list`
   - 페이징 조회: `/api/boards?size=100`
   - 검색 성능: `/api/boards/search?keyword=test`
3. **메모리 마운트 vs 일반 디스크**: 동일한 작업을 수행하여 응답 시간 비교

이 문서를 참고하여 MySQL 메모리 마운트 성능 비교 테스트를 진행해보세요!
