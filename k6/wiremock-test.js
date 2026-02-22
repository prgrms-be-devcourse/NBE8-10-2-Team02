/**
 * WireMock 방어체계 테스트
 *
 * WireMock → IGDB 503 → Retry 4회(최대 ~7s) → CB 3번 실패 → CB OPEN → 즉시 DB fallback
 *
 * 전제 조건:
 *   1. docker compose -f docker-compose-monitoring.yml up wiremock -d
 *   2. application-dev.yml의 igdb.base-url 주석 해제:
 *        igdb:
 *          base-url: "http://localhost:8081"
 *   3. 앱 재시작 (Caffeine 인메모리 캐시 초기화)
 *   4. k6 run k6/wiremock-test.js
 *
 * 관찰 포인트 (Grafana 대시보드 19004):
 *   - CircuitBreaker State: CLOSED(0) → OPEN(1) 전환 시점
 *   - Retry calls: 재시도 급증 구간
 *   - http_req_duration: 초기 ~7s(Retry 소진) → CB OPEN 후 <100ms(DB fallback)
 *
 * 시나리오:
 *   0~20s : 0 → 5명 (CB OPEN 유도 — 3번 실패면 CB 열림)
 *   20~60s: 5명 유지 (CB OPEN 확인, DB fallback 안정성 관찰)
 *   60~70s: 5 → 10명 (Bulkhead max=10 한계 테스트)
 *   70~80s: 10 → 0명
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 5  },
        { duration: '40s', target: 5  },
        { duration: '10s', target: 10 },
        { duration: '10s', target: 0  },
    ],
    thresholds: {
        // DB fallback 덕분에 에러율은 낮아야 함
        http_req_failed: ['rate<0.05'],
    },
};

const BASE_URL = 'http://localhost:8080';

// video 엔드포인트: igdbId별 캐시 → 고유 ID로 캐시 미스 유도
// (앱 재시작 후 Caffeine 캐시가 비어있어야 함)
const GAME_IDS = [
    81221, 44856, 114419, 181407, 256422, 391506, 102272, 245313, 273833, 235524,
    297664, 187882, 15086, 166565, 104780, 260489, 237496, 101979, 105069, 73992,
    5816, 177286, 268021, 350308, 264594, 295795, 365421, 273501, 279141, 313625,
    142331, 216281, 161129, 174452, 197006, 246265, 349230, 379802, 227165, 281495,
    332192, 316334, 313933, 292911, 203497, 385752, 291736, 119728, 147563, 312463,
    59092, 199207, 287294, 78771, 367158, 9816, 333439, 53520, 233752, 325313,
    252860, 250189, 278982, 366118, 92092, 221407, 162708, 162770, 383454, 183429,
    318078, 5755,
];

export default function () {
    // VU·ITER 조합으로 다른 igdbId 순환 → 캐시 미스 극대화
    const igdbId = GAME_IDS[(__VU * __ITER + __VU) % GAME_IDS.length];

    // /video 엔드포인트: 캐시 미스 시 IgdbCircuitBreakerClient → WireMock 503 → Retry → CB 집계
    const res = http.get(`${BASE_URL}/api/v1/games/${igdbId}/video`, {
        tags: { endpoint: 'video' },
        timeout: '30s',  // Retry 4회 최대 ~7s + 여유
    });

    check(res, {
        // CB CLOSED: WireMock 503 → Retry 소진 → fallback(null→빈 응답) = 200
        // CB OPEN  : 즉시 fallback(null→빈 응답) = 200
        'status 200 (fallback)': (r) => r.status === 200,
    });

    // RateLimiter(4/s) 감안 — 너무 빠르게 쏘면 RequestNotPermitted
    sleep(0.5);
}
