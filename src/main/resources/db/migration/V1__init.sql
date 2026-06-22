CREATE TABLE health_check (
                              id SERIAL PRIMARY KEY,
                              status VARCHAR(50) NOT NULL DEFAULT 'ok',
                              checked_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO health_check (status) VALUES ('ok');