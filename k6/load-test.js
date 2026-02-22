/**
 * Load Test - 단계적 부하 증가 (캐시 효과, 응답시간 추이 확인)
 * 실행: k6 run k6/load-test.js
 *
 * 시나리오:
 *  0~30s  : 1명 → 20명 (워밍업, 캐시 채워지는 구간)
 *  30~90s : 20명 유지 (캐시 hit rate 높아지는 구간)
 *  90~120s: 20명 → 50명 (부하 증가)
 *  120~150s: 50명 유지 (최대 부하)
 *  150~180s: 50명 → 0명 (종료)
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '60s', target: 20 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // P95 응답시간 1초 이내
        http_req_failed: ['rate<0.05'],    // 에러율 5% 이하
    },
};

const BASE_URL = 'http://localhost:8080';
const GAME_IDS = [8042, 314265, 15536, 113360, 194558];

export default function () {
    const igdbId = GAME_IDS[Math.floor(Math.random() * GAME_IDS.length)];

    // 인기 게임 (캐시 효과가 가장 잘 보이는 엔드포인트)
    const popularRes = http.get(`${BASE_URL}/api/v1/games/popular/igdb`, {
        tags: { endpoint: 'popular' },
    });
    check(popularRes, { 'popular 200': (r) => r.status === 200 });

    // 게임 상세 (igdbId별 캐시)
    const detailRes = http.get(`${BASE_URL}/api/v1/games/${igdbId}`, {
        tags: { endpoint: 'detail' },
    });
    check(detailRes, { 'detail 200': (r) => r.status === 200 });

    sleep(1);
}
