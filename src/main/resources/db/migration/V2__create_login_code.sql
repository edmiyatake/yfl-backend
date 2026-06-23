CREATE TABLE login_code (
                            id BIGSERIAL PRIMARY KEY,
                            email VARCHAR(255) NOT NULL,
                            code VARCHAR(6) NOT NULL,
                            expires_at TIMESTAMP NOT NULL,
                            consumed_at TIMESTAMP,
                            attempts INT NOT NULL DEFAULT 0,
                            created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_login_code_email ON login_code (email);