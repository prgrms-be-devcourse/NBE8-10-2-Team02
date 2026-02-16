-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 게임 피처 벡터 컬럼 추가 (160차원: Genre(20) + Theme(30) + Keyword(100) + Mode(5) + Perspective(5))
ALTER TABLE game ADD COLUMN feature_vector vector(160);

-- 유저 프로필 벡터 컬럼 추가
ALTER TABLE member ADD COLUMN profile_vector vector(160);

-- HNSW 인덱스 (코사인 유사도 기반)
CREATE INDEX ix_game_feature_vector ON game USING hnsw (feature_vector vector_cosine_ops);
