-- V2__add_game_attributes.sql
-- 게임 엔티티 확장: 테마, 키워드, 게임모드, 플레이어 시점, 회사, 외부 ID

-- 1. 마스터 테이블
CREATE TABLE theme (
    id BIGSERIAL PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_theme_igdb_id UNIQUE (igdb_id)
);

CREATE TABLE keyword (
    id BIGSERIAL PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_keyword_igdb_id UNIQUE (igdb_id)
);

CREATE TABLE game_mode (
    id BIGSERIAL PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_game_mode_igdb_id UNIQUE (igdb_id)
);

CREATE TABLE player_perspective (
    id BIGSERIAL PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_player_perspective_igdb_id UNIQUE (igdb_id)
);

CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_company_igdb_id UNIQUE (igdb_id)
);

-- 2. 중간 테이블
CREATE TABLE game_theme (
    id BIGSERIAL PRIMARY KEY,
    game_id INT NOT NULL,
    theme_id BIGINT NOT NULL,
    CONSTRAINT uk_game_theme_game_theme UNIQUE (game_id, theme_id),
    CONSTRAINT fk_game_theme_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_theme_theme FOREIGN KEY (theme_id) REFERENCES theme(id) ON DELETE CASCADE
);

CREATE TABLE game_keyword (
    id BIGSERIAL PRIMARY KEY,
    game_id INT NOT NULL,
    keyword_id BIGINT NOT NULL,
    CONSTRAINT uk_game_keyword_game_keyword UNIQUE (game_id, keyword_id),
    CONSTRAINT fk_game_keyword_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_keyword_keyword FOREIGN KEY (keyword_id) REFERENCES keyword(id) ON DELETE CASCADE
);

CREATE TABLE game_game_mode (
    id BIGSERIAL PRIMARY KEY,
    game_id INT NOT NULL,
    game_mode_id BIGINT NOT NULL,
    CONSTRAINT uk_game_game_mode_game_game_mode UNIQUE (game_id, game_mode_id),
    CONSTRAINT fk_game_game_mode_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_game_mode_game_mode FOREIGN KEY (game_mode_id) REFERENCES game_mode(id) ON DELETE CASCADE
);

CREATE TABLE game_player_perspective (
    id BIGSERIAL PRIMARY KEY,
    game_id INT NOT NULL,
    player_perspective_id BIGINT NOT NULL,
    CONSTRAINT uk_game_player_perspective_game_pp UNIQUE (game_id, player_perspective_id),
    CONSTRAINT fk_game_player_perspective_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_player_perspective_pp FOREIGN KEY (player_perspective_id) REFERENCES player_perspective(id) ON DELETE CASCADE
);

CREATE TABLE game_company (
    id BIGSERIAL PRIMARY KEY,
    game_id INT NOT NULL,
    company_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT uk_game_company_game_company_role UNIQUE (game_id, company_id, role),
    CONSTRAINT fk_game_company_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_company_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
);

CREATE TABLE game_external_id (
    id BIGSERIAL PRIMARY KEY,
    game_id INT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    CONSTRAINT uk_game_external_id_game_platform_eid UNIQUE (game_id, platform, external_id),
    CONSTRAINT fk_game_external_id_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
);

CREATE INDEX ix_game_external_id_platform_eid ON game_external_id (platform, external_id);

-- 3. game 테이블에 컬럼 추가
ALTER TABLE game ADD COLUMN storyline TEXT;
ALTER TABLE game ADD COLUMN aggregated_rating DOUBLE PRECISION;
ALTER TABLE game ADD COLUMN franchise_igdb_id BIGINT;
ALTER TABLE game ADD COLUMN franchise_name VARCHAR(255);
