CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255),
    CONSTRAINT chk_roles_name CHECK (name IN ('USER', 'ADMIN', 'OPERATOR'))
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(80),
    last_name VARCHAR(80),
    avatar_url VARCHAR(1000),
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'LOCKED', 'PENDING_VERIFICATION'))
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_hash VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL,
    media_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    poster_path VARCHAR(1000),
    backdrop_path VARCHAR(1000),
    overview TEXT,
    release_date DATE,
    vote_average NUMERIC(4,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_favorites_media_type CHECK (media_type IN ('MOVIE', 'TV')),
    CONSTRAINT uk_favorites_user_media UNIQUE (user_id, media_type, media_id)
);

CREATE TABLE watchlist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL,
    media_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    poster_path VARCHAR(1000),
    backdrop_path VARCHAR(1000),
    overview TEXT,
    release_date DATE,
    vote_average NUMERIC(4,2),
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_watchlist_media_type CHECK (media_type IN ('MOVIE', 'TV')),
    CONSTRAINT chk_watchlist_status CHECK (status IN ('PLANNED', 'WATCHING', 'COMPLETED', 'DROPPED')),
    CONSTRAINT uk_watchlist_user_media UNIQUE (user_id, media_type, media_id)
);

CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL,
    media_id BIGINT NOT NULL,
    score NUMERIC(3,1) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_ratings_media_type CHECK (media_type IN ('MOVIE', 'TV')),
    CONSTRAINT chk_ratings_score CHECK (score >= 0.5 AND score <= 10.0),
    CONSTRAINT uk_ratings_user_media UNIQUE (user_id, media_type, media_id)
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL,
    media_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    spoiler_flag BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_reviews_media_type CHECK (media_type IN ('MOVIE', 'TV'))
);

CREATE TABLE membership_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(80) NOT NULL UNIQUE,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    duration_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT chk_membership_duration CHECK (duration_type IN ('FREE', 'MONTHLY', 'YEARLY')),
    CONSTRAINT chk_membership_price CHECK (price >= 0)
);

CREATE TABLE user_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES membership_plans(id) ON DELETE RESTRICT,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT chk_user_memberships_status CHECK (status IN ('ACTIVE', 'CANCELED', 'EXPIRED'))
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(120),
    metadata_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_favorites_user_created ON favorites(user_id, created_at DESC);
CREATE INDEX idx_favorites_media ON favorites(media_type, media_id);
CREATE INDEX idx_watchlist_user_updated ON watchlist_items(user_id, updated_at DESC);
CREATE INDEX idx_watchlist_media ON watchlist_items(media_type, media_id);
CREATE INDEX idx_ratings_user_updated ON ratings(user_id, updated_at DESC);
CREATE INDEX idx_ratings_media ON ratings(media_type, media_id);
CREATE INDEX idx_reviews_media_created ON reviews(media_type, media_id, created_at DESC);
CREATE INDEX idx_memberships_user_status ON user_memberships(user_id, status);
CREATE INDEX idx_audit_logs_user_created ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_action_created ON audit_logs(action, created_at DESC);

INSERT INTO roles (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'USER', 'Default authenticated user'),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN', 'Administrator with platform management access'),
    ('00000000-0000-0000-0000-000000000003', 'OPERATOR', 'Operational support role')
ON CONFLICT (name) DO NOTHING;

INSERT INTO membership_plans (id, name, description, price, currency, duration_type, active) VALUES
    ('00000000-0000-0000-0000-000000000101', 'Free', 'Default free access for personal lists and reviews.', 0.00, 'USD', 'FREE', true),
    ('00000000-0000-0000-0000-000000000102', 'Plus', 'Future-ready monthly membership plan scaffold.', 4.99, 'USD', 'MONTHLY', true),
    ('00000000-0000-0000-0000-000000000103', 'Pro', 'Future-ready yearly membership plan scaffold.', 49.99, 'USD', 'YEARLY', true)
ON CONFLICT (name) DO NOTHING;
