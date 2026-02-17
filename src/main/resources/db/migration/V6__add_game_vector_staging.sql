-- 벡터 벌크 업데이트용 스테이징 테이블
-- 배치에서 TRUNCATE → batch INSERT → UPDATE game JOIN staging → TRUNCATE 패턴으로 사용
CREATE TABLE game_vector_staging (
    game_id      INT  NOT NULL,
    feature_vector TEXT NOT NULL
);
