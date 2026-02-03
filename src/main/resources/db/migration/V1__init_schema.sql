-- V1__init_schema.sql
-- 초기 스키마 생성

-- 1. 독립 테이블 (FK 없음)
CREATE TABLE member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    api_key VARCHAR(255) UNIQUE,
    nickname VARCHAR(30) NOT NULL UNIQUE,
    create_date DATETIME(6),
    modify_date DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE genre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_genre_igdb_id UNIQUE (igdb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE platform (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_platform_igdb_id UNIQUE (igdb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE game (
    id INT AUTO_INCREMENT PRIMARY KEY,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255),
    summary VARCHAR(5000) NOT NULL,
    cover_image_id VARCHAR(255),
    first_release_date DATE,
    last_fetched_at DATETIME(6),
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    review_count BIGINT DEFAULT 0,
    CONSTRAINT uk_game_igdb_id UNIQUE (igdb_id),
    INDEX ix_game_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. game 관련 연결 테이블
CREATE TABLE game_developer (
    game_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_game_developer_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE game_publisher (
    game_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_game_publisher_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE game_genre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    genre_id BIGINT NOT NULL,
    CONSTRAINT uk_game_genre_game_genre UNIQUE (game_id, genre_id),
    CONSTRAINT fk_game_genre_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_genre_genre FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE game_platform (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    platform_id BIGINT NOT NULL,
    CONSTRAINT uk_game_platform_game_platform UNIQUE (game_id, platform_id),
    CONSTRAINT fk_game_platform_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_platform_platform FOREIGN KEY (platform_id) REFERENCES platform(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. review (game, member 참조)
CREATE TABLE review (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content VARCHAR(255),
    rating DOUBLE,
    create_date DATETIME(6),
    modify_date DATETIME(6),
    game_id INT,
    author_id INT,
    CONSTRAINT fk_review_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE SET NULL,
    CONSTRAINT fk_review_author FOREIGN KEY (author_id) REFERENCES member(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. member_game (member, game, review 참조)
CREATE TABLE member_game (
    id INT AUTO_INCREMENT PRIMARY KEY,
    platform_id BIGINT,
    playtime DOUBLE,
    is_favorite BIT(1),
    status VARCHAR(50),
    member_id INT,
    game_id INT,
    review_id INT,
    CONSTRAINT fk_member_game_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_game_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_game_review FOREIGN KEY (review_id) REFERENCES review(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. game_like (member, game 참조)
CREATE TABLE game_like (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    game_id INT NOT NULL,
    created_at DATETIME(6),
    CONSTRAINT uk_game_like_member_game UNIQUE (member_id, game_id),
    INDEX ix_game_like_game_id (game_id),
    INDEX ix_game_like_member_id (member_id),
    CONSTRAINT fk_game_like_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_like_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. post (member 참조)
CREATE TABLE post (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    create_date DATETIME(6),
    modify_date DATETIME(6),
    view_count INT NOT NULL DEFAULT 0,
    author_id INT,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES member(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. post_comment (post, member, self 참조)
CREATE TABLE post_comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255),
    create_date DATETIME(6),
    modify_date DATETIME(6),
    deleted BIT(1) DEFAULT 0,
    post_id INT,
    parent_id INT,
    author_id INT,
    CONSTRAINT fk_post_comment_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_comment_parent FOREIGN KEY (parent_id) REFERENCES post_comment(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_comment_author FOREIGN KEY (author_id) REFERENCES member(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. post_like (post, member 참조)
CREATE TABLE post_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id INT,
    member_id INT,
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_like_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. post_tag (post, tag 참조)
CREATE TABLE post_tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT,
    tag_id INT,
    CONSTRAINT uk_post_tag UNIQUE (post_id, tag_id),
    CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
