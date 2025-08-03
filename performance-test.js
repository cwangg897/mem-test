import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// 커스텀 메트릭
export let errorRate = new Rate('errors');

// 테스트 설정: 50 VU, 3분 동안 일정 부하 유지
export let options = {
  vus: 50,
  duration: '3m',
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% 요청 500ms 이하
    http_req_failed: ['rate<0.05'],   // 에러율 5% 이하
    errors: ['rate<0.05'],
  },
};

// 베이스 URL 설정
const BASE_URL = 'http://localhost:8080';

// 랜덤 데이터 생성 함수들
const titlePrefixes = ['성능테스트', '부하테스트', 'K6테스트', '벤치마크', '스트레스테스트'];
const contentWords = [
  'MySQL', '메모리', '성능', '테스트', 'K6', '부하', '벤치마크', '데이터베이스',
  '최적화', '인덱스', '쿼리', '트랜잭션', '커넥션풀', '캐시', 'Spring Boot'
];

function generateRandomTitle() {
  const prefix = titlePrefixes[Math.floor(Math.random() * titlePrefixes.length)];
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 10000);
  return `${prefix} ${timestamp}-${random}`;
}

function generateRandomContent() {
  const wordCount = 20 + Math.floor(Math.random() * 80); // 20-100 단어
  let content = '';
  for (let i = 0; i < wordCount; i++) {
    content += contentWords[Math.floor(Math.random() * contentWords.length)];
    if (i < wordCount - 1) content += ' ';
    if (Math.random() < 0.1) content += '. ';
  }
  return content;
}

function generateRandomAuthor() {
  return `k6user${Math.floor(Math.random() * 1000)}`;
}

// 메인 테스트 함수: 20% 쓰기, 80% 읽기 분기
export default function () {
  const rand = Math.random();

  if (rand < 0.2) {
    // 20% 확률 쓰기 (POST)
    const createPayload = JSON.stringify({
      title: generateRandomTitle(),
      content: generateRandomContent(),
      author: generateRandomAuthor(),
    });
    const createParams = {
      headers: { 'Content-Type': 'application/json' },
    };
    const createResponse = http.post(`${BASE_URL}/api/boards`, createPayload, createParams);
    const createSuccess = check(createResponse, {
      'create status is 200 or 201': (r) => r.status === 200 || r.status === 201,
      'create response time < 1000ms': (r) => r.timings.duration < 1000,
    });
    if (!createSuccess) errorRate.add(1);
  } else {
    // 80% 확률 읽기 (GET)
    const listResponse = http.get(`${BASE_URL}/api/boards?page=0&size=20`);
    const listSuccess = check(listResponse, {
      'list status is 200': (r) => r.status === 200,
      'list response time < 500ms': (r) => r.timings.duration < 500,
      'list has data': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data && (Array.isArray(data) || (data.content && Array.isArray(data.content)));
        } catch (e) {
          return false;
        }
      },
    });
    if (!listSuccess) errorRate.add(1);

    try {
      const data = JSON.parse(listResponse.body);
      let id = null;
      if (Array.isArray(data)) {
        id = data.length > 0 ? data[0].id : null;
      } else if (data.content && Array.isArray(data.content)) {
        id = data.content.length > 0 ? data.content[0].id : null;
      }
      if (id) {
        const detailResponse = http.get(`${BASE_URL}/api/boards/${id}`);
        const detailSuccess = check(detailResponse, {
          'detail status is 200': (r) => r.status === 200,
          'detail response time < 300ms': (r) => r.timings.duration < 300,
        });
        if (!detailSuccess) errorRate.add(1);
      }
    } catch (e) {
      // 무시
    }
  }

  sleep(Math.random() * 1); // 0~1초 랜덤 대기
}

// setup 함수 (테스트 시작 시 1회 실행)
export function setup() {
  console.log('=== K6 성능 테스트 시작 ===');
  console.log('Base URL:', BASE_URL);
  const healthCheck = http.get(`${BASE_URL}/actuator/health`);
  if (healthCheck.status !== 200) {
    console.warn('Health check failed:', healthCheck.status);
  }
  return { startTime: Date.now() };
}

// teardown 함수 (테스트 종료 시 1회 실행)
export function teardown(data) {
  const endTime = Date.now();
  const duration = (endTime - data.startTime) / 1000;
  console.log('=== K6 성능 테스트 완료 ===');
  console.log(`총 실행 시간: ${duration}초`);
}
