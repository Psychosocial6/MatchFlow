-- Spring Boot uses ;; to leave semicolons inside PostgreSQL DO blocks intact.
DO $$
BEGIN
    CREATE TYPE enum_participation_type AS ENUM ('SOLO', 'TEAM');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

DO $$
BEGIN
    CREATE TYPE enum_format AS ENUM ('SINGLE_ELIMINATION', 'DOUBLE_ELIMINATION', 'ROUND_ROBIN', 'SWISS');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

DO $$
BEGIN
    CREATE TYPE enum_status_tournaments AS ENUM ('DRAFT', 'REGISTRATION', 'ONGOING', 'FINISHED', 'CANCELLED');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

DO $$
BEGIN
    CREATE TYPE enum_status_applications AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

DO $$
BEGIN
    CREATE TYPE enum_status_match AS ENUM ('SCHEDULED', 'LIVE', 'FINISHED', 'CANCELLED');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

DO $$
BEGIN
    CREATE TYPE enum_status_game AS ENUM ('PENDING', 'LIVE', 'FINISHED');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

DO $$
BEGIN
    CREATE TYPE enum_event_type AS ENUM ('TECH_LOSS', 'CANCEL', 'DISQUALIFICATION');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    name VARCHAR(255),
    surname VARCHAR(255),
    vk_id VARCHAR(255) UNIQUE,
    avatar_url VARCHAR(255),
    CONSTRAINT chk_users_auth_method CHECK (password IS NOT NULL OR vk_id IS NOT NULL),
    CONSTRAINT chk_users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);;

ALTER TABLE users ALTER COLUMN email DROP NOT NULL;;

DO $$
BEGIN
    ALTER TABLE users ADD CONSTRAINT chk_users_password_email
        CHECK (password IS NULL OR email IS NOT NULL);
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(16) NOT NULL DEFAULT 'USER'
        CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'USER'));;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verification_required BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP WITH TIME ZONE;;

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    user_id INTEGER PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);;

CREATE TABLE IF NOT EXISTS sports (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_cybersport BOOLEAN NOT NULL,
    supports_faceit BOOLEAN NOT NULL
);;

CREATE TABLE IF NOT EXISTS teams (
    id SERIAL PRIMARY KEY,
    captain_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    tag VARCHAR(10) UNIQUE,
    creation_date TIMESTAMP NOT NULL,
    CONSTRAINT chk_teams_tag_length CHECK (char_length(tag) >= 2),
    CONSTRAINT fk_teams_captain FOREIGN KEY (captain_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);;

CREATE TABLE IF NOT EXISTS team_members (
    id SERIAL PRIMARY KEY,
    team_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT uq_team_members_team_user UNIQUE (team_id, user_id),
    CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES teams (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);;

CREATE TABLE IF NOT EXISTS tournaments (
    id SERIAL PRIMARY KEY,
    admin_id INTEGER NOT NULL,
    sport_id INTEGER NOT NULL,
    title VARCHAR(255),
    regulations TEXT,
    participation_type enum_participation_type NOT NULL,
    format enum_format NOT NULL,
    status enum_status_tournaments NOT NULL,
    reg_start_date TIMESTAMP NOT NULL,
    reg_end_date TIMESTAMP NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    max_participants INTEGER NOT NULL,
    CONSTRAINT chk_tournaments_dates_timeline CHECK (
        reg_start_date < reg_end_date AND reg_end_date <= start_date AND start_date <= end_date
    ),
    CONSTRAINT chk_tournaments_min_participants CHECK (max_participants >= 2),
    CONSTRAINT fk_tournaments_admin FOREIGN KEY (admin_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_tournaments_sport FOREIGN KEY (sport_id) REFERENCES sports (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);;

CREATE TABLE IF NOT EXISTS referees (
    id SERIAL PRIMARY KEY,
    tournament_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT uq_referees_tournament_user UNIQUE (tournament_id, user_id),
    CONSTRAINT fk_referees_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_referees_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);;

CREATE TABLE IF NOT EXISTS tournament_applications (
    id SERIAL PRIMARY KEY,
    tournament_id INTEGER NOT NULL,
    user_id INTEGER,
    team_id INTEGER,
    status enum_status_applications NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    CONSTRAINT chk_ta_participant_xor CHECK (
        (user_id IS NOT NULL AND team_id IS NULL) OR (user_id IS NULL AND team_id IS NOT NULL)
    ),
    CONSTRAINT fk_ta_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ta_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_ta_team FOREIGN KEY (team_id) REFERENCES teams (id)
        ON UPDATE CASCADE ON DELETE SET NULL
);;

CREATE TABLE IF NOT EXISTS matches (
    id SERIAL PRIMARY KEY,
    tournament_id INTEGER NOT NULL,
    participant1 INTEGER,
    participant2 INTEGER,
    winner_id INTEGER,
    bracket_stage VARCHAR(255),
    round_number INTEGER,
    score1 INTEGER,
    score2 INTEGER,
    status enum_status_match NOT NULL,
    winner_next_match_id INTEGER,
    loser_next_match_id INTEGER,
    start_date TIMESTAMP NOT NULL,
    CONSTRAINT chk_matches_participants_distinct CHECK (
        participant1 IS NULL OR participant2 IS NULL OR participant1 <> participant2
    ),
    CONSTRAINT chk_matches_scores_non_negative CHECK (
        (score1 IS NULL AND score2 IS NULL) OR (score1 >= 0 AND score2 >= 0)
    ),
    CONSTRAINT chk_matches_winner_validity CHECK (
        winner_id IS NULL OR (participant1 IS NOT NULL AND winner_id = participant1)
            OR (participant2 IS NOT NULL AND winner_id = participant2)
    ),
    CONSTRAINT chk_matches_round_positive CHECK (round_number > 0),
    CONSTRAINT fk_matches_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_matches_participant1 FOREIGN KEY (participant1) REFERENCES tournament_applications (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_matches_participant2 FOREIGN KEY (participant2) REFERENCES tournament_applications (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_matches_winner FOREIGN KEY (winner_id) REFERENCES tournament_applications (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_matches_winner_next_match FOREIGN KEY (winner_next_match_id) REFERENCES matches (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_matches_loser_next_match FOREIGN KEY (loser_next_match_id) REFERENCES matches (id)
        ON UPDATE CASCADE ON DELETE SET NULL
);;

CREATE TABLE IF NOT EXISTS match_games (
    id SERIAL PRIMARY KEY,
    match_id INTEGER NOT NULL,
    game_number INTEGER NOT NULL,
    score1 INTEGER NOT NULL,
    score2 INTEGER NOT NULL,
    winner_id INTEGER,
    status enum_status_game NOT NULL,
    faceit_id VARCHAR(255),
    CONSTRAINT uq_mg_match_game_number UNIQUE (match_id, game_number),
    CONSTRAINT chk_mg_values_valid CHECK (game_number > 0 AND score1 >= 0 AND score2 >= 0),
    CONSTRAINT fk_mg_match FOREIGN KEY (match_id) REFERENCES matches (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_mg_winner FOREIGN KEY (winner_id) REFERENCES tournament_applications (id)
        ON UPDATE CASCADE ON DELETE SET NULL
);;

CREATE TABLE IF NOT EXISTS match_protocols (
    id SERIAL PRIMARY KEY,
    match_id INTEGER NOT NULL,
    event_type enum_event_type NOT NULL,
    description TEXT,
    created_by INTEGER NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    CONSTRAINT chk_mp_description_not_empty CHECK (length(trim(description)) > 0),
    CONSTRAINT fk_mp_match FOREIGN KEY (match_id) REFERENCES matches (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_mp_created_by FOREIGN KEY (created_by) REFERENCES referees (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);;

CREATE TABLE IF NOT EXISTS tournament_results (
    id SERIAL PRIMARY KEY,
    tournament_id INTEGER NOT NULL,
    participant_id INTEGER,
    place INTEGER,
    CONSTRAINT uq_tr_tournament_participant UNIQUE (tournament_id, participant_id),
    CONSTRAINT chk_tr_place_positive CHECK (place > 0),
    CONSTRAINT fk_tr_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_tr_participant FOREIGN KEY (participant_id) REFERENCES tournament_applications (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);;
