import http from 'k6/http';
import { check, sleep } from 'k6';

// ======= 테스트 실행을 위한 값 설정 =======
const BASE_URL = 'https://athena-local.i-am-jay.com';

// 테스트할 쿠폰 ID를 직접 지정
const TEST_COUPON_ID = 95;
// ================================================

export const options = {
  vus: 1, // JMeter ThreadGroup.num_threads // 반복 횟수
  duration: '30s', // 테스트 시간
  rampingVus: [
    { duration: '1s', target: 100 }, // JMeter ramp_time 1초
    { duration: '29s', target: 100 }, // 나머지 시간 유지
  ],
};

export default function () {
  const couponId = TEST_COUPON_ID;

  // 각 VU별로 고유한 사용자 생성을 위한 랜덤값
  const userSuffix = Math.random().toString(36).substring(7);
  const email = `loadtest_user_${__VU}_${userSuffix}@example.com`;
  const password = '123456';

  // 1. 사용자 생성
  const userCreatePayload = JSON.stringify({
    email: email,
    password: password,
    nickname: `LoadTest User ${__VU}`,
  });

  const userCreateParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const userCreateRes = http.post(`${BASE_URL}/api/user`, userCreatePayload, userCreateParams);

  check(userCreateRes, {
    'user creation status is 200 or 201': (r) => r.status === 200 || r.status === 201,
  });

  // 사용자 생성이 실패한 경우 테스트 종료
  if (userCreateRes.status !== 200 && userCreateRes.status !== 201) {
    console.log(`사용자 생성 실패: ${userCreateRes.status}`);
    return;
  }

  sleep(0.1); // 사용자 생성 후 잠시 대기

  // 2. 사용자 로그인
  const loginPayload = JSON.stringify({
    email: email,
    password: password
  });

  const loginParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const loginRes = http.post(`${BASE_URL}/api/user/login`, loginPayload, loginParams);

  check(loginRes, {
    'login status is 200': (r) => r.status === 200,
  });

  // 로그인이 실패한 경우 테스트 종료
  if (loginRes.status !== 200) {
    console.log(`로그인 실패: ${loginRes.status}`);
    return;
  }

  // 토큰 추출
  const accessToken = loginRes.json().accessToken;

  if (!accessToken) {
    console.log('액세스 토큰을 찾을 수 없습니다');
    return;
  }

  sleep(0.1); // 로그인 후 잠시 대기

  // 3. 쿠폰 발급 (메인 테스트 대상)
  const couponPayload = JSON.stringify({
    couponId: couponId
  });

  const couponParams = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    },
  };

  const couponRes = http.post(`${BASE_URL}/api/userCoupon`, couponPayload, couponParams);

  check(couponRes, {
    'coupon issuance status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'coupon response time < 1000ms': (r) => r.timings.duration < 1000,
  });

  // 결과 로깅 (필요시)
  if (couponRes.status !== 200 && couponRes.status !== 201) {
    console.log(`쿠폰 발급 실패: ${couponRes.status}, 응답: ${couponRes.body}`);
    console.log(`쿠폰 ID: ${couponId}`);
  }

  sleep(0.1); // 다음 반복 전 대기
}
