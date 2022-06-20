-- 전체 데이터베이스 조회
SELECT datname FROM pg_database;


-- 사용자가 생성한 데이터베이스만 조회(아래 두 개 동일)
SELECT datname FROM pg_database WHERE datistemplate = false;
SELECT * FROM pg_tables WHERE schemaname = 'public';


-- 전체 테이블 조회(show tables)
SELECT tablename FROM pg_tables;


-- 사용자가 생성한 테이블만 조회(아래 두 개 동일)
SELECT * FROM pg_tables WHERE schemaname = 'public';
SELECT RELNAME AS TABLE_NAME FROM PG_STAT_USER_TABLES;


-- 컬럼 목록 조회(desc table명)
SELECT * FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_CATALOG = 'yapp' AND TABLE_NAME = 'folder'
ORDER BY ORDINAL_POSITION;

SELECT *
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'folder'
ORDER BY ordinal_position;


-- 컬럼 디폴트값 설정
ALTER TABLE folder ALTER COLUMN bookmark_count SET DEFAULT 0;
ALTER TABLE folder ALTER COLUMN created_at SET DEFAULT now();
commit;


-- id sequence(auto_increment) reset
ALTER SEQUENCE folder_id_seq RESTART WITH 1;


SELECT * FROM folder;
commit;

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 0, '자식폴더 1-1', 1);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 1, '자식폴더 1-2', 1);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 2, '자식폴더 1-3', 1);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 3, '자식폴더 1-4', 1);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 0, '자식폴더 2-1', 2);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 1, '자식폴더 2-2', 2);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 0, '자식폴더 2-1-1', 9);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 1, '자식폴더 2-1-2', 9);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 2, '자식폴더 2-1-3', 9);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 1, '자식폴더 2-1-2', 9);

INSERT INTO folder (emoji, index, name, parent_id)
VALUES ('😷', 0, '자식폴더 3-1', 3);

INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 1);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 2);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 3);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 4);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 5);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 6);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 7);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 8);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 9);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 10);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 11);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 12);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 13);
INSERT INTO account_folder (account_id, folder_id)
VALUES (1, 14);


/* RDBMS Self References Problem -> Change to FK DELETE CASCADE */

-- Look up the current FK definition
SELECT pg_get_constraintdef(oid) AS constraint_def
FROM   pg_constraint
WHERE  conrelid = 'public.folder'::regclass  -- assuming public schema
AND    conname = 'fkn0cjh1seljcp0mc4tj1ufh99m';
-- result: "FOREIGN KEY (parent_id) REFERENCES folder(id)"

ALTER TABLE folder
DROP CONSTRAINT fkn0cjh1seljcp0mc4tj1ufh99m,
	ADD CONSTRAINT fkn0cjh1seljcp0mc4tj1ufh99m
	FOREIGN KEY (parent_id) REFERENCES folder (id) ON DELETE CASCADE;


SELECT pg_get_constraintdef(oid) AS constraint_def
FROM   pg_constraint
WHERE  conrelid = 'public.account_folder'::regclass  -- assuming public schema
AND    conname = 'fkq12fenyrw4agbqxdkoe9j9t0';
-- result: "FOREIGN KEY (folder_id) REFERENCES folder(id)"

ALTER TABLE account_folder
DROP CONSTRAINT fkq12fenyrw4agbqxdkoe9j9t0,
	ADD CONSTRAINT fkq12fenyrw4agbqxdkoe9j9t0
	FOREIGN KEY (folder_id) REFERENCES folder (id) ON DELETE CASCADE;

-- 테이블 생성 쿼리 확인
SELECT
        'CREATE TABLE ' || relname || E'\n(\n' ||
  array_to_string(
    array_agg(
      '    ' || column_name || ' ' ||  type || ' '|| not_null
    )
    , E',\n'
  ) || E'\n);\n'
from
    (
        SELECT
            c.relname, a.attname AS column_name,
            pg_catalog.format_type(a.atttypid, a.atttypmod) as type,
            case
                when a.attnotnull
                    then 'NOT NULL'
                else 'NULL'
                END as not_null
        FROM pg_class c,
             pg_attribute a,
             pg_type t
        WHERE c.relname = 'folder'
          AND a.attnum > 0
          AND a.attrelid = c.oid
          AND a.atttypid = t.oid
        ORDER BY a.attnum
    ) as tabledefinition
group by relname;
