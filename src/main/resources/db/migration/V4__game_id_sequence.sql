-- Game 테이블 ID 전략을 IDENTITY -> SEQUENCE로 변경
-- IDENTITY는 INSERT 시점에 DB가 ID를 생성하므로 Hibernate JDBC 배치가 불가능
-- SEQUENCE는 INSERT 전에 nextval()로 ID를 선점하므로 JDBC 배치 INSERT 가능

-- V1에서 game.id를 SERIAL로 생성했으므로 game_id_seq 시퀀스가 이미 존재함
-- INCREMENT BY를 50으로 변경하여 Hibernate allocationSize=50과 맞춤
ALTER SEQUENCE game_id_seq INCREMENT BY 50;
