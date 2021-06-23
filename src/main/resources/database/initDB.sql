--DROP TABLE IF EXISTS templates;
CREATE TABLE IF NOT EXISTS templates(
                         id serial PRIMARY KEY,
                         filename VARCHAR(255)
);