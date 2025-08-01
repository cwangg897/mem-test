import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// 커스텀 메트릭
export let errorRate = new Rate('errors');

// 테스트 설정
export let options = {
  scenarios: {
    // 점진적 부하 증가 시나리오
    ramping_load: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '30s', target: 10 },   // 30초 동안 10명까지 증가
        { duration: '1m', target: 50 },    // 1분 동안 50명까지 증가
        { duration: '2m', target: 100 },   // 2분 동안 100명까지 증가
        { duration: '1m', target: 100 },   // 1분 동안 100명 유지
        { duration: '30s', target: 0 },    // 30초 동안 0명까지 감소
      ],
    },
    // 일정한 부하 시나리오
    constant_load: {
      executor: 'constant-vus',
      vus: 50,
      duration: '3m',
      startTime: '5m', // ramping_load 시나리오 후에 실행
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이하
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
    
    // 가끔 문장 구분
    if (Math.random() < 0.1) content += '. ';
  }
  
  return content;
}

function generateRandomAuthor() {
  return `k6user${Math.floor(Math.random() * 1000)}`;
}

// 메인 테스트 함수
export default function () {
  // 게시글 생성 테스트
  const createPayload = JSON.stringify({
    title: generateRandomTitle(),
    content: generateRandomContent(),
    author: generateRandomAuthor()
  });

  const createParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const createResponse = http.post(`${BASE_URL}/api/boards`, createPayload, createParams);
  
  const createSuccess = check(createResponse, {
    'create status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'create response time < 1000ms': (r) => r.timings.duration < 1000,
  });

  if (!createSuccess) {
    errorRate.add(1);
    console.log(`Create failed: ${createResponse.status} - ${createResponse.body}`);
  }

  // 게시글 목록 조회 테스트 (페이징)
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
    }
  });

  if (!listSuccess) {
    errorRate.add(1);
    console.log(`List failed: ${listResponse.status} - ${listResponse.body}`);
  }

  // 특정 게시글 조회 테스트 (생성된 게시글이 있다면)
  if (createSuccess && createResponse.status <= 201) {
    try {
      const createdBoard = JSON.parse(createResponse.body);
      if (createdBoard.id) {
        const detailResponse = http.get(`${BASE_URL}/api/boards/${createdBoard.id}`);
        
        const detailSuccess = check(detailResponse, {
          'detail status is 200': (r) => r.status === 200,
          'detail response time < 300ms': (r) => r.timings.duration < 300,
        });

        if (!detailSuccess) {
          errorRate.add(1);
        }
      }
    } catch (e) {
      // JSON 파싱 실패 시 무시
    }
  }

  // 요청 간 간격
  sleep(Math.random() * 2); // 0-2초 랜덤 대기
}

// 테스트 시작/종료 시 실행되는 함수들
export function setup() {
  console.log('=== K6 성능 테스트 시작 ===');
  console.log('Base URL:', BASE_URL);
  
  // 서버 상태 확인
  const healthCheck = http.get(`${BASE_URL}/actuator/health`);
  if (healthCheck.status !== 200) {
    console.warn('Health check failed:', healthCheck.status);
  }
  
  return { startTime: Date.now() };
}

export function teardown(data) {
  const endTime = Date.now();
  const duration = (endTime - data.startTime) / 1000;
  
  console.log('=== K6 성능 테스트 완료 ===');
  console.log(`총 실행 시간: ${duration}초`);
}
