/**
 * Resilience4j 방어체계 테스트
 * - RateLimiter: 초당 4회 제한 → throttle 확인
 * - Bulkhead: 동시 10개 제한 → 11번째부터 DB fallback
 * - CircuitBreaker: 실패율 40% 초과 시 OPEN
 *
 * 실행: k6 run k6/resilience-test.js
 *
 * 시나리오:
 *  0~10s : 0 → 30명 (빠르게 올려서 Bulkhead/RateLimiter 즉시 자극)
 *  10~60s: 30명 유지 (방어체계 동작 관찰 구간)
 *  60~70s: 30 → 0명
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 30 },
        { duration: '50s', target: 30 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        // fallback이 있으므로 에러율은 낮아야 함
        http_req_failed: ['rate<0.05'],
    },
};

const BASE_URL = 'http://localhost:8080';

// 캐시 미스를 유도하기 위한 다양한 igdbId (DB에서 조회한 값)
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
    // VU마다 다른 igdbId를 순환해서 캐시 미스 유도
    const igdbId = GAME_IDS[(__VU * __ITER + __VU) % GAME_IDS.length];

    const res = http.get(`${BASE_URL}/api/v1/games/${igdbId}`, {
        tags: { endpoint: 'detail' },
    });

    check(res, {
        'status 200 (IGDB or DB fallback)': (r) => r.status === 200,
    });

    // sleep 없음 → 최대한 빠르게 쏘아서 방어체계 자극
}
