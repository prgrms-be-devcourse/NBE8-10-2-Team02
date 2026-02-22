/**
 * Smoke Test - 기본 동작 확인 (소수 유저, 짧은 시간)
 * 실행: k6 run k6/smoke-test.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 5,          // 동시 사용자 5명
    duration: '30s', // 30초
};

const BASE_URL = 'http://localhost:8080';

// 테스트할 igdbId 목록 (DB에 실제 존재하는 ID로 바꿔주세요)
const GAME_IDS = [1942, 1020, 11133, 7346, 113112];

export default function () {
    // 1. 인기 게임 조회 (igdbPopularGamesCache - 첫 요청 후 캐시됨)
    const popularRes = http.get(`${BASE_URL}/api/v1/games/popular/igdb`);
    check(popularRes, {
        'popular games status 200': (r) => r.status === 200,
        'popular games response time < 500ms': (r) => r.timings.duration < 500,
    });

    // 2. 게임 상세 조회 (gameDetailCache - igdbId별 캐시)
    const igdbId = GAME_IDS[Math.floor(Math.random() * GAME_IDS.length)];
    const detailRes = http.get(`${BASE_URL}/api/v1/games/${igdbId}`);
    check(detailRes, {
        'game detail status 200': (r) => r.status === 200,
        'game detail response time < 300ms': (r) => r.timings.duration < 300,
    });

    // 3. 게임 검색
    const searchRes = http.get(`${BASE_URL}/api/v1/games/search?query=zelda`);
    check(searchRes, {
        'search status 200': (r) => r.status === 200,
    });

    sleep(1);
}
