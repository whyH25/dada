-- ============================================================
--  InterviewAI — Database DDL (수정본)
--  MySQL 8.x | UTF8MB4 | InnoDB | 총 27개 테이블
--
--  수정 내역
--  - REFERENCES user (user_id) → REFERENCES users (user_idx)  [전체]
--  - REFERENCES resume (resume_id) → REFERENCES user_resume (resume_id)
--  - REFERENCES top100_interview_room → REFERENCES interview_room_top100
--  - INDEX idx_user_status: ON user (status) → ON users (user_status)
--  - orphan 컬럼 라인 제거 (role, status, access_token, refresh_token)
--  - interview_room에 interview_type, ai_applicant_count 컬럼 추가
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;


-- ============================================================
--  1. 회원 관리
-- ============================================================

CREATE TABLE users (
    user_idx         BIGINT          NOT NULL AUTO_INCREMENT,
    user_email       VARCHAR(100)    NOT NULL,
    user_pwd         VARCHAR(255)    NULL     COMMENT '소셜 계정은 NULL',
    user_name        VARCHAR(50)     NOT NULL,
    user_phone       VARCHAR(20)     NULL,
    user_profile_img VARCHAR(500)    NULL     COMMENT 'URL',
    user_status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '회원 상태: ACTIVE(활성), INACTIVE(비활성), DELETED(탈퇴)',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at       DATETIME        NULL     COMMENT '소프트 삭제',
    PRIMARY KEY (user_idx),
    UNIQUE KEY uq_user_email (user_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 정보 테이블';


CREATE TABLE users_social (
    social_idx          BIGINT          NOT NULL AUTO_INCREMENT,
    user_idx            BIGINT          NOT NULL,
    provider            ENUM('GOOGLE','KAKAO','NAVER') NOT NULL,
    provider_account_id VARCHAR(200)    NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (social_idx),
    UNIQUE KEY uq_social_provider (provider, provider_account_id),
    CONSTRAINT fk_users_social FOREIGN KEY (user_idx) REFERENCES users (user_idx) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 소셜 로그인 연동 테이블';


CREATE TABLE user_resume (
    resume_id   BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    title       VARCHAR(200)    NOT NULL DEFAULT '이력서',
    file_url    VARCHAR(500)    NULL,
    file_name   VARCHAR(200)    NULL,
    parsed_text LONGTEXT        NULL         COMMENT 'AI 파싱 텍스트',
    keywords    JSON            NULL         COMMENT '추출 키워드 배열',
    is_primary  TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (resume_id),
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES users (user_idx) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이력서';


CREATE TABLE user_portfolio (
    portfolio_id    BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    file_url        VARCHAR(500)    NULL,
    link_url        VARCHAR(500)    NULL     COMMENT 'GitHub 등 외부 URL',
    description     TEXT            NULL,
    parsed_text     LONGTEXT        NULL     COMMENT 'AI 파싱 텍스트',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (portfolio_id),
    CONSTRAINT fk_portfolio_user FOREIGN KEY (user_id) REFERENCES users (user_idx) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='포트폴리오';


CREATE TABLE cover_letter (
    cl_id       BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    title       VARCHAR(200)    NOT NULL DEFAULT '자기소개서',
    content     LONGTEXT        NULL,
    file_url    VARCHAR(500)    NULL,
    file_name   VARCHAR(200)    NULL,
    is_primary  TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cl_id),
    CONSTRAINT fk_cl_user FOREIGN KEY (user_id) REFERENCES users (user_idx) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='자기소개서';


-- ============================================================
--  2. 공통 코드
-- ============================================================

CREATE TABLE category_job (
    job_id      INT             NOT NULL AUTO_INCREMENT,
    job_code    VARCHAR(50)     NOT NULL,
    job_name    VARCHAR(100)    NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (job_id),
    UNIQUE KEY uq_job_code (job_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='직무 분류 30종';


CREATE TABLE category_industry (
    industry_id     INT             NOT NULL AUTO_INCREMENT,
    industry_code   VARCHAR(50)     NOT NULL,
    industry_name   VARCHAR(100)    NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (industry_id),
    UNIQUE KEY uq_industry_code (industry_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='업종 분류 21종';


-- ============================================================
--  3. 면접방
-- ============================================================

CREATE TABLE interview_room_top100 (
    room_id             BIGINT          NOT NULL AUTO_INCREMENT,
    company_name        VARCHAR(200)    NOT NULL,
    company_logo_url    VARCHAR(500)    NULL,
    industry_id         INT             NULL,
    job_id              INT             NULL,
    difficulty          ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'MEDIUM',
    interviewer_count   INT             NOT NULL DEFAULT 3,
    description         TEXT            NULL,
    is_active           TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id),
    CONSTRAINT fk_top100_industry FOREIGN KEY (industry_id) REFERENCES category_industry (industry_id),
    CONSTRAINT fk_top100_job      FOREIGN KEY (job_id)      REFERENCES category_job      (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='100대 기업 면접방';


CREATE TABLE interview_room (
    room_id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id              BIGINT          NOT NULL,
    company_name         VARCHAR(200)    NULL,
    job_id               INT             NULL,
    industry_id          INT             NULL,
    interview_type       ENUM('NEW','EXPERIENCED','INTERN') NOT NULL DEFAULT 'NEW' COMMENT '신입/경력/인턴',
    difficulty           ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'MEDIUM',
    interviewer_count    INT             NOT NULL DEFAULT 1,
    ai_applicant_count   INT             NOT NULL DEFAULT 0,
    has_ai_applicant     TINYINT(1)      NOT NULL DEFAULT 0,
    resume_id            BIGINT          NULL,
    portfolio_id         BIGINT          NULL,
    cl_id                BIGINT          NULL,
    status               ENUM('READY','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'READY',
    created_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id),
    CONSTRAINT fk_room_user      FOREIGN KEY (user_id)      REFERENCES users             (user_idx),
    CONSTRAINT fk_room_job       FOREIGN KEY (job_id)       REFERENCES category_job      (job_id),
    CONSTRAINT fk_room_industry  FOREIGN KEY (industry_id)  REFERENCES category_industry (industry_id),
    CONSTRAINT fk_room_resume    FOREIGN KEY (resume_id)    REFERENCES user_resume       (resume_id),
    CONSTRAINT fk_room_portfolio FOREIGN KEY (portfolio_id) REFERENCES user_portfolio    (portfolio_id),
    CONSTRAINT fk_room_cl        FOREIGN KEY (cl_id)        REFERENCES cover_letter      (cl_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 생성 면접방';


-- ============================================================
--  4. AI 페르소나
-- ============================================================

CREATE TABLE ai_interviewer (
    interviewer_id  BIGINT          NOT NULL AUTO_INCREMENT,
    room_id         BIGINT          NULL     COMMENT 'interview_room.room_id',
    top100_room_id  BIGINT          NULL     COMMENT 'interview_room_top100.room_id',
    persona_type    ENUM('HR','FIELD','EXECUTIVE') NOT NULL,
    display_name    VARCHAR(100)    NOT NULL,
    personality     TEXT            NULL,
    system_prompt   TEXT            NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (interviewer_id),
    CONSTRAINT fk_interviewer_room   FOREIGN KEY (room_id)       REFERENCES interview_room       (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_interviewer_top100 FOREIGN KEY (top100_room_id)REFERENCES interview_room_top100(room_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 면접관 페르소나';


CREATE TABLE ai_applicant (
    applicant_id    BIGINT          NOT NULL AUTO_INCREMENT,
    room_id         BIGINT          NULL,
    top100_room_id  BIGINT          NULL,
    job_id          INT             NULL,
    industry_id     INT             NULL,
    display_name    VARCHAR(100)    NOT NULL,
    career_years    INT             NULL,
    background      TEXT            NULL,
    strength        TEXT            NULL,
    system_prompt   TEXT            NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (applicant_id),
    CONSTRAINT fk_applicant_room     FOREIGN KEY (room_id)       REFERENCES interview_room        (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_applicant_top100   FOREIGN KEY (top100_room_id)REFERENCES interview_room_top100 (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_applicant_job      FOREIGN KEY (job_id)        REFERENCES category_job          (job_id),
    CONSTRAINT fk_applicant_industry FOREIGN KEY (industry_id)   REFERENCES category_industry     (industry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 경쟁 지원자';


-- ============================================================
--  5. 면접 진행
-- ============================================================

CREATE TABLE interview_session (
    session_id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id            BIGINT      NOT NULL,
    room_id            BIGINT      NULL,
    top100_room_id     BIGINT      NULL,
    started_at         DATETIME    NULL,
    ended_at           DATETIME    NULL,
    total_duration_sec INT         NULL,
    status             ENUM('IN_PROGRESS','COMPLETED','ABORTED') NOT NULL DEFAULT 'IN_PROGRESS',
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id),
    CONSTRAINT fk_session_user   FOREIGN KEY (user_id)       REFERENCES users                (user_idx),
    CONSTRAINT fk_session_room   FOREIGN KEY (room_id)       REFERENCES interview_room       (room_id),
    CONSTRAINT fk_session_top100 FOREIGN KEY (top100_room_id)REFERENCES interview_room_top100(room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='면접 진행 세션';


CREATE TABLE interview_scenario (
    scenario_id        BIGINT      NOT NULL AUTO_INCREMENT,
    session_id         BIGINT      NOT NULL,
    interviewer_id     BIGINT      NULL,
    parent_scenario_id BIGINT      NULL     COMMENT '꼬리질문 원본 ID',
    question_type      ENUM('COMMON','PERSONAL','FOLLOWUP') NOT NULL,
    question_text      TEXT        NOT NULL,
    question_order     INT         NOT NULL DEFAULT 0,
    is_asked           TINYINT(1)  NOT NULL DEFAULT 0,
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (scenario_id),
    CONSTRAINT fk_scenario_session     FOREIGN KEY (session_id)         REFERENCES interview_session  (session_id) ON DELETE CASCADE,
    CONSTRAINT fk_scenario_interviewer FOREIGN KEY (interviewer_id)     REFERENCES ai_interviewer     (interviewer_id),
    CONSTRAINT fk_scenario_parent      FOREIGN KEY (parent_scenario_id) REFERENCES interview_scenario (scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='면접 시나리오 / 질문 목록';


CREATE TABLE interview_log (
    log_id       BIGINT          NOT NULL AUTO_INCREMENT,
    session_id   BIGINT          NOT NULL,
    scenario_id  BIGINT          NULL,
    speaker_type ENUM('USER','AI_INTERVIEWER','AI_APPLICANT') NOT NULL,
    speaker_id   BIGINT          NULL,
    content_text TEXT            NULL,
    stt_raw      TEXT            NULL     COMMENT 'STT 원문 (보정 전)',
    audio_url    VARCHAR(500)    NULL,
    video_url    VARCHAR(500)    NULL,
    turn_order   INT             NOT NULL DEFAULT 0,
    spoken_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_log_session  FOREIGN KEY (session_id)  REFERENCES interview_session  (session_id) ON DELETE CASCADE,
    CONSTRAINT fk_log_scenario FOREIGN KEY (scenario_id) REFERENCES interview_scenario (scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='면접 대화 로그';


-- ============================================================
--  6. 분석 & 피드백
-- ============================================================

CREATE TABLE report_behavior_analysis (
    analysis_id         BIGINT      NOT NULL AUTO_INCREMENT,
    session_id          BIGINT      NOT NULL,
    log_id              BIGINT      NULL,
    speech_rate_wpm     FLOAT       NULL     COMMENT '말 속도 (단어/분)',
    silence_total_sec   FLOAT       NULL     COMMENT '총 침묵 시간(초)',
    longest_silence_sec FLOAT       NULL     COMMENT '최장 침묵 구간(초)',
    filler_word_count   INT         NULL     COMMENT '필러 단어 수',
    eye_contact_score   FLOAT       NULL     COMMENT '시선 처리 점수 (0~100)',
    answer_duration_sec FLOAT       NULL     COMMENT '답변 소요 시간(초)',
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (analysis_id),
    CONSTRAINT fk_analysis_session FOREIGN KEY (session_id) REFERENCES interview_session (session_id) ON DELETE CASCADE,
    CONSTRAINT fk_analysis_log     FOREIGN KEY (log_id)     REFERENCES interview_log     (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='행동 / 정량 분석';


CREATE TABLE report_feedback (
    report_id        BIGINT      NOT NULL AUTO_INCREMENT,
    session_id       BIGINT      NOT NULL,
    user_id          BIGINT      NOT NULL,
    overall_score    FLOAT       NULL,
    logic_score      FLOAT       NULL,
    expression_score FLOAT       NULL,
    attitude_score   FLOAT       NULL,
    technical_score  FLOAT       NULL,
    summary_comment  TEXT        NULL,
    strengths        TEXT        NULL,
    improvements     TEXT        NULL,
    radar_data       JSON        NULL     COMMENT '레이더 차트 데이터',
    generated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (report_id),
    UNIQUE KEY uq_report_session (session_id),
    CONSTRAINT fk_report_session FOREIGN KEY (session_id) REFERENCES interview_session (session_id),
    CONSTRAINT fk_report_user    FOREIGN KEY (user_id)    REFERENCES users             (user_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='종합 피드백 리포트';


CREATE TABLE report_feedback_detail (
    detail_id          BIGINT      NOT NULL AUTO_INCREMENT,
    report_id          BIGINT      NOT NULL,
    scenario_id        BIGINT      NULL,
    log_id             BIGINT      NULL,
    quality_score      FLOAT       NULL,
    logic_comment      TEXT        NULL,
    expression_comment TEXT        NULL,
    improvement_tip    TEXT        NULL,
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (detail_id),
    CONSTRAINT fk_detail_report   FOREIGN KEY (report_id)  REFERENCES report_feedback    (report_id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_scenario FOREIGN KEY (scenario_id)REFERENCES interview_scenario (scenario_id),
    CONSTRAINT fk_detail_log      FOREIGN KEY (log_id)     REFERENCES interview_log      (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='질문별 정성 분석';


-- ============================================================
--  7. 공지사항
-- ============================================================

CREATE TABLE notice (
    notice_id  BIGINT          NOT NULL AUTO_INCREMENT,
    admin_id   BIGINT          NOT NULL,
    type       ENUM('NOTICE','FAQ') NOT NULL DEFAULT 'NOTICE',
    title      VARCHAR(300)    NOT NULL,
    content    LONGTEXT        NOT NULL,
    is_pinned  TINYINT(1)      NOT NULL DEFAULT 0,
    view_count INT             NOT NULL DEFAULT 0,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME        NULL,
    PRIMARY KEY (notice_id),
    CONSTRAINT fk_notice_admin FOREIGN KEY (admin_id) REFERENCES users (user_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='공지사항 / FAQ';


-- ============================================================
--  8. 커뮤니티
-- ============================================================

CREATE TABLE category_community (
    category_id INT             NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(300)    NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    is_active   TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (category_id),
    UNIQUE KEY uq_community_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='커뮤니티 게시판 카테고리';


CREATE TABLE community_post (
    post_id       BIGINT          NOT NULL AUTO_INCREMENT,
    user_id       BIGINT          NOT NULL,
    category_id   INT             NOT NULL,
    title         VARCHAR(300)    NOT NULL,
    content       LONGTEXT        NOT NULL,
    view_count    INT             NOT NULL DEFAULT 0,
    like_count    INT             NOT NULL DEFAULT 0,
    comment_count INT             NOT NULL DEFAULT 0,
    is_pinned     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at    DATETIME        NULL,
    PRIMARY KEY (post_id),
    CONSTRAINT fk_post_user     FOREIGN KEY (user_id)    REFERENCES users              (user_idx),
    CONSTRAINT fk_post_category FOREIGN KEY (category_id)REFERENCES category_community (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='커뮤니티 게시글';


CREATE TABLE community_comment (
    comment_id        BIGINT      NOT NULL AUTO_INCREMENT,
    post_id           BIGINT      NOT NULL,
    user_id           BIGINT      NOT NULL,
    parent_comment_id BIGINT      NULL     COMMENT '대댓글 원본 ID',
    content           TEXT        NOT NULL,
    like_count        INT         NOT NULL DEFAULT 0,
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        DATETIME    NULL,
    PRIMARY KEY (comment_id),
    CONSTRAINT fk_comment_post   FOREIGN KEY (post_id)           REFERENCES community_post    (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user   FOREIGN KEY (user_id)           REFERENCES users             (user_idx),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES community_comment (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='댓글 / 대댓글';


CREATE TABLE community_like (
    like_id     BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    target_type ENUM('POST','COMMENT') NOT NULL,
    target_id   BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (like_id),
    UNIQUE KEY uq_like (user_id, target_type, target_id),
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users (user_idx) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글 / 댓글 좋아요';


-- ============================================================
--  9. 채팅
-- ============================================================

CREATE TABLE chat_room (
    room_id         BIGINT          NOT NULL AUTO_INCREMENT,
    room_type       ENUM('DIRECT','GROUP') NOT NULL,
    room_name       VARCHAR(200)    NULL     COMMENT '그룹 채팅방 이름',
    created_by      BIGINT          NOT NULL,
    last_message    TEXT            NULL,
    last_message_at DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id),
    CONSTRAINT fk_chatroom_creator FOREIGN KEY (created_by) REFERENCES users (user_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='채팅방';


CREATE TABLE chat_room_member (
    member_id BIGINT      NOT NULL AUTO_INCREMENT,
    room_id   BIGINT      NOT NULL,
    user_id   BIGINT      NOT NULL,
    joined_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at   DATETIME    NULL,
    is_active TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (member_id),
    UNIQUE KEY uq_room_member (room_id, user_id),
    CONSTRAINT fk_member_room FOREIGN KEY (room_id) REFERENCES chat_room (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users     (user_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='채팅방 참여자';


CREATE TABLE chat_message (
    message_id   BIGINT          NOT NULL AUTO_INCREMENT,
    room_id      BIGINT          NOT NULL,
    sender_id    BIGINT          NOT NULL,
    content      TEXT            NOT NULL,
    message_type ENUM('TEXT','IMAGE','FILE') NOT NULL DEFAULT 'TEXT',
    file_url     VARCHAR(500)    NULL,
    is_deleted   TINYINT(1)      NOT NULL DEFAULT 0,
    sent_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id),
    CONSTRAINT fk_message_room   FOREIGN KEY (room_id)   REFERENCES chat_room (room_id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users     (user_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='채팅 메시지';


CREATE TABLE chat_message_read (
    read_id    BIGINT      NOT NULL AUTO_INCREMENT,
    message_id BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    read_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (read_id),
    UNIQUE KEY uq_message_read (message_id, user_id),
    CONSTRAINT fk_read_message FOREIGN KEY (message_id) REFERENCES chat_message (message_id) ON DELETE CASCADE,
    CONSTRAINT fk_read_user    FOREIGN KEY (user_id)    REFERENCES users        (user_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메시지 읽음 처리';


-- ============================================================
--  인덱스
-- ============================================================

CREATE INDEX idx_user_status        ON users                (user_status, deleted_at);
CREATE INDEX idx_room_user_status   ON interview_room       (user_id, status);
CREATE INDEX idx_session_user       ON interview_session    (user_id);
CREATE INDEX idx_session_room       ON interview_session    (room_id);
CREATE INDEX idx_scenario_session   ON interview_scenario   (session_id, question_order);
CREATE INDEX idx_log_session_order  ON interview_log        (session_id, turn_order);
CREATE INDEX idx_report_user        ON report_feedback      (user_id, generated_at DESC);
CREATE INDEX idx_post_category      ON community_post       (category_id, created_at DESC);
CREATE INDEX idx_post_user          ON community_post       (user_id);
CREATE INDEX idx_comment_post       ON community_comment    (post_id, created_at ASC);
CREATE INDEX idx_chat_message_room  ON chat_message         (room_id, sent_at DESC);
CREATE INDEX idx_notice_type_pinned ON notice               (type, is_pinned, created_at DESC);


-- ============================================================
--  기초 데이터: 직무 30종
-- ============================================================
INSERT INTO category_job (job_code, job_name, sort_order) VALUES
('DEV_BACKEND',     '백엔드 개발자',           1),
('DEV_FRONTEND',    '프론트엔드 개발자',         2),
('DEV_FULLSTACK',   '풀스택 개발자',            3),
('DEV_MOBILE',      '모바일 앱 개발자',          4),
('DEV_AI',          'AI/ML 엔지니어',           5),
('DEV_DATA',        '데이터 엔지니어',           6),
('DEV_DEVOPS',      'DevOps/클라우드 엔지니어',   7),
('DEV_SECURITY',    '보안 엔지니어',             8),
('DEV_EMBEDDED',    '임베디드 개발자',           9),
('DEV_GAME',        '게임 개발자',              10),
('DATA_ANALYST',    '데이터 분석가',            11),
('DATA_SCIENTIST',  '데이터 사이언티스트',       12),
('PM',              '프로덕트 매니저',           13),
('PO',              '프로덕트 오너',             14),
('DESIGN_UX',       'UX 디자이너',              15),
('DESIGN_UI',       'UI 디자이너',              16),
('DESIGN_BRAND',    '브랜드 디자이너',           17),
('MKT_DIGITAL',     '디지털 마케터',             18),
('MKT_CONTENT',     '콘텐츠 마케터',             19),
('MKT_BRAND',       '브랜드 마케터',             20),
('SALES',           '영업 담당자',              21),
('CS',              '고객 서비스',              22),
('HR',              '인사/채용 담당자',           23),
('FINANCE',         '재무/회계',               24),
('STRATEGY',        '전략기획',                25),
('SUPPLY_CHAIN',    '공급망/물류',              26),
('QA',              'QA 엔지니어',             27),
('RESEARCH',        'R&D 연구원',              28),
('LEGAL',           '법무',                   29),
('CONSULTING',      '컨설턴트',               30);


-- ============================================================
--  기초 데이터: 업종 21종
-- ============================================================
INSERT INTO category_industry (industry_code, industry_name, sort_order) VALUES
('IT_SW',           'IT/소프트웨어',             1),
('IT_INTERNET',     '인터넷/이커머스',            2),
('IT_GAME',         '게임',                     3),
('FINANCE_BANK',    '금융/은행',                 4),
('FINANCE_INSURE',  '보험',                     5),
('FINANCE_FINTECH', '핀테크',                    6),
('MANUFACTURE',     '제조/자동차',               7),
('SEMICONDUCTOR',   '반도체/전자',               8),
('BIOTECH',         '바이오/제약',               9),
('HEALTHCARE',      '의료/헬스케어',             10),
('MEDIA',           '미디어/엔터테인먼트',         11),
('EDUCATION',       '교육',                    12),
('RETAIL',          '유통/소비재',               13),
('FOOD_BEVERAGE',   '식음료',                   14),
('CONSTRUCTION',    '건설/부동산',               15),
('ENERGY',          '에너지/환경',               16),
('LOGISTICS',       '물류/운송',                17),
('CONSULTING',      '컨설팅/전문서비스',           18),
('GOVERNMENT',      '공공/비영리',               19),
('STARTUP',         '스타트업',                 20),
('OTHER',           '기타',                    21);


-- ============================================================
--  기초 데이터: 커뮤니티 카테고리
-- ============================================================
INSERT INTO category_community (name, description, sort_order) VALUES
('면접 후기',   '실제 면접 경험 공유',          1),
('정보 공유',   '취업 정보 및 팁 공유',         2),
('질문 & 답변', '면접 준비 관련 질문',          3),
('자유',       '자유롭게 이야기해요',           4);


SET FOREIGN_KEY_CHECKS = 1;
