-- V3__alter_sequences_and_text_columns.sql
-- 1) IDENTITY -> SEQUENCE 전략 전환을 위해 시퀀스 INCREMENT를 50으로 변경
--    Hibernate allocationSize=50과 맞춰야 ID 충돌이 발생하지 않음
-- 2) summary 컬럼 VARCHAR(5000) -> TEXT 변경

-- summary VARCHAR(5000) -> TEXT (V1에서 VARCHAR(5000)으로 생성됨)
ALTER TABLE game ALTER COLUMN summary TYPE TEXT;

-- V1에서 생성된 조인 테이블
ALTER SEQUENCE game_genre_id_seq INCREMENT BY 50;
ALTER SEQUENCE game_platform_id_seq INCREMENT BY 50;

-- V2에서 생성된 테이블
ALTER SEQUENCE game_theme_id_seq INCREMENT BY 50;
ALTER SEQUENCE game_keyword_id_seq INCREMENT BY 50;
ALTER SEQUENCE game_game_mode_id_seq INCREMENT BY 50;
ALTER SEQUENCE game_player_perspective_id_seq INCREMENT BY 50;
ALTER SEQUENCE game_company_id_seq INCREMENT BY 50;
ALTER SEQUENCE game_external_id_id_seq INCREMENT BY 50;
ALTER SEQUENCE keyword_id_seq INCREMENT BY 50;
ALTER SEQUENCE company_id_seq INCREMENT BY 50;
