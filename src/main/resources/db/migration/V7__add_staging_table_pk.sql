-- game_vector_staging에 PK 추가 (UPSERT용 ON CONFLICT 대상)
ALTER TABLE game_vector_staging ADD CONSTRAINT game_vector_staging_pkey PRIMARY KEY (game_id);
