# MySQL 성능 비교 테스트 가이드

## 1. 사전 데이터 생성

### 멀티스레드로 1000개 테스트 데이터 생성
```bash
# IntelliJ에서 실행하거나 터미널에서 실행
./gradlew test --tests BoardDataInsertTest.insertBoardDataWithMultiThread
```

또는 IntelliJ에서:
- `src/test/java/com/sosimple/BoardDataInsertTest.java` 파일 열기
- `insertBoardDataWithMultiThread` 메소드 실행

## 2. K6 성능 테스트 실행

### K6 설치 (MacOS)
```bash
brew install k6
```

### K6 설치 (Windows)
```bash
winget install k6
```

### 성능 테스트 실행
```bash
# 애플리케이션 실행 후
k6 run performance-test.js
```

## 3. 테스트 시나리오

### 데이터 생성 테스트
- **스레드 풀**: 10개 스레드
- **생성 데이터**: 1000개 게시글
- **측정 항목**: 총 실행 시간, 평균 처리 시간

### K6 성능 테스트
- **점진적 부하**: 1명 → 100명 사용자 (5분)
- **일정 부하**: 50명 사용자 지속 (3분)
- **테스트 API**: 
  - POST /api/boards (게시글 생성)
  - GET /api/boards (목록 조회)
  - GET /api/boards/{id} (상세 조회)

### 성능 임계값
- 95% 요청이 500ms 이하
- 에러율 5% 이하

## 4. MySQL 메모리 마운트 설정

### 일반 MySQL vs 메모리 MySQL 비교
1. **일반 MySQL**로 먼저 테스트 실행
2. **메모리 마운트 MySQL**로 설정 변경 후 테스트 실행
3. 결과 비교 분석

### Docker로 메모리 MySQL 실행 예시
```bash
# 메모리 기반 MySQL
docker run -d \
  --name mysql-memory \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=sosimple \
  -p 3306:3306 \
  --tmpfs /var/lib/mysql:rw,noexec,nosuid,size=1g \
  mysql:8.0

# 일반 MySQL
docker run -d \
  --name mysql-disk \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=sosimple \
  -p 3307:3306 \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0
```

## 5. 결과 분석 포인트

### 데이터 생성 성능
- 총 실행 시간 비교
- 평균 처리 시간 비교
- 스레드별 처리량 비교

### K6 테스트 결과
- HTTP 요청 응답 시간 (p95, p99)
- 초당 처리량 (RPS)
- 에러율
- 동시 사용자별 성능 변화

### 예상 결과
- **메모리 MySQL**: 더 빠른 INSERT/SELECT 성능
- **일반 MySQL**: 디스크 I/O로 인한 성능 제약
- **차이점**: 특히 대량 INSERT 시 큰 차이 예상
