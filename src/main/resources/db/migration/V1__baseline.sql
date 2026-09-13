-- V1__baseline.sql
-- Baseline schema for homeclock

-- ==================== USERS ====================
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       PRIMARY KEY,
    kakao_id        VARCHAR(255)    NOT NULL UNIQUE,
    nickname        VARCHAR(50),
    name            VARCHAR(50),
    profile_image   VARCHAR(500),
    email           VARCHAR(100),
    gender          VARCHAR(10),
    age_range       VARCHAR(10),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    withdrawn_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL
);

-- ==================== USER PROFILES ====================
CREATE TABLE IF NOT EXISTS user_profiles (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL UNIQUE,
    birth_year          INTEGER,
    annual_income_enc   BYTEA,
    current_assets_enc  BYTEA,
    take_home_ratio     NUMERIC(4, 3)   DEFAULT 0.840,
    saving_rate         NUMERIC(4, 3),
    savings_apr         NUMERIC(5, 4)   DEFAULT 0.0300,
    house_price_growth  NUMERIC(5, 4)   DEFAULT 0.0200,
    loan_ltv            NUMERIC(4, 3)   DEFAULT 0.000,
    job_category        VARCHAR(40),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ==================== USER PROFILE HISTORIES ====================
CREATE TABLE IF NOT EXISTS user_profile_histories (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL,
    annual_income_enc   BYTEA,
    current_assets_enc  BYTEA,
    saving_rate         NUMERIC(4, 3),
    loan_ltv            NUMERIC(4, 3),
    changed_at          TIMESTAMP       NOT NULL,
    CONSTRAINT fk_profile_history_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_profile_history_user_changed
    ON user_profile_histories (user_id, changed_at DESC);

-- ==================== CONSENT LOGS ====================
CREATE TABLE IF NOT EXISTS consent_logs (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    consent_type    VARCHAR(30)     NOT NULL,
    terms_version   VARCHAR(20),
    agreed          BOOLEAN         NOT NULL,
    agreed_at       TIMESTAMP       NOT NULL,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(300),
    CONSTRAINT fk_consent_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_consent_user_type_agreed
    ON consent_logs (user_id, consent_type, agreed_at DESC);

-- ==================== REGIONS ====================
CREATE TABLE IF NOT EXISTS regions (
    lawd_cd     VARCHAR(5)      PRIMARY KEY,
    sido        VARCHAR(30)     NOT NULL,
    sigungu     VARCHAR(50)     NOT NULL,
    center_lat  NUMERIC(10, 7),
    center_lng  NUMERIC(10, 7)
);

CREATE INDEX IF NOT EXISTS idx_region_sido
    ON regions (sido);

-- ==================== APT COMPLEXES ====================
CREATE TABLE IF NOT EXISTS apt_complexes (
    id              BIGSERIAL       PRIMARY KEY,
    lawd_cd         VARCHAR(5)      NOT NULL,
    dong            VARCHAR(50)     NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    built_year      INTEGER,
    lat             NUMERIC(10, 7),
    lng             NUMERIC(10, 7),
    geocode_status  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    geocoded_at     TIMESTAMP,
    CONSTRAINT uq_apt_complex_lawd_dong_name UNIQUE (lawd_cd, dong, name)
);

CREATE INDEX IF NOT EXISTS idx_apt_complex_lawd
    ON apt_complexes (lawd_cd);

CREATE INDEX IF NOT EXISTS idx_apt_complex_lat_lng
    ON apt_complexes (lat, lng);

-- ==================== APT TRADES ====================
CREATE TABLE IF NOT EXISTS apt_trades (
    id              BIGSERIAL       PRIMARY KEY,
    complex_id      BIGINT          NOT NULL,
    exclusive_area  NUMERIC(7, 2),
    floor           INTEGER,
    deal_amount     BIGINT          NOT NULL,
    deal_date       DATE            NOT NULL,
    canceled        BOOLEAN         NOT NULL DEFAULT FALSE,
    source_hash     VARCHAR(64)     NOT NULL UNIQUE,
    created_at      TIMESTAMP       NOT NULL,
    CONSTRAINT fk_apt_trade_complex FOREIGN KEY (complex_id) REFERENCES apt_complexes (id)
);

CREATE INDEX IF NOT EXISTS idx_apt_trade_complex_date
    ON apt_trades (complex_id, deal_date DESC);

-- ==================== SYNC LOGS ====================
CREATE TABLE IF NOT EXISTS sync_logs (
    id              BIGSERIAL       PRIMARY KEY,
    lawd_cd         VARCHAR(5)      NOT NULL,
    deal_ymd        VARCHAR(6)      NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    row_count       INTEGER,
    error_message   VARCHAR(500),
    synced_at       TIMESTAMP       NOT NULL,
    CONSTRAINT uq_sync_log_lawd_ymd UNIQUE (lawd_cd, deal_ymd)
);

-- ==================== CALCULATION HISTORIES ====================
CREATE TABLE IF NOT EXISTS calculation_histories (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT,
    complex_id      BIGINT,
    lawd_cd         VARCHAR(5)      NOT NULL,
    exclusive_area  NUMERIC(7, 2),
    house_price     BIGINT          NOT NULL,
    total_months    INTEGER,
    reachable       BOOLEAN         NOT NULL,
    inputs_json     JSONB,
    created_at      TIMESTAMP       NOT NULL,
    CONSTRAINT fk_calc_history_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_calc_history_user_created
    ON calculation_histories (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_calc_history_lawd_created
    ON calculation_histories (lawd_cd, created_at DESC);

-- ==================== SHARE CARDS ====================
CREATE TABLE IF NOT EXISTS share_cards (
    id              BIGSERIAL       PRIMARY KEY,
    share_key       VARCHAR(22)     NOT NULL UNIQUE,
    user_id         BIGINT,
    sigungu         VARCHAR(50)     NOT NULL,
    exclusive_area  NUMERIC(7, 2),
    total_months    INTEGER,
    reachable       BOOLEAN         NOT NULL,
    meme_text       VARCHAR(200)    NOT NULL,
    image_url       VARCHAR(500),
    view_count      INTEGER         NOT NULL DEFAULT 0,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL,
    CONSTRAINT fk_share_card_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_share_card_user_created
    ON share_cards (user_id, created_at DESC);

-- ==================== INCOME STAT SNAPSHOTS ====================
CREATE TABLE IF NOT EXISTS income_stat_snapshots (
    id                  BIGSERIAL       PRIMARY KEY,
    period              VARCHAR(7)      NOT NULL,
    lawd_cd             VARCHAR(5),
    sample_size         INTEGER         NOT NULL,
    income_p25          BIGINT,
    income_p50          BIGINT,
    income_p75          BIGINT,
    avg_months          INTEGER,
    unreachable_ratio   NUMERIC(5, 4),
    created_at          TIMESTAMP       NOT NULL,
    CONSTRAINT uq_income_stat_period_lawd UNIQUE (period, lawd_cd)
);
