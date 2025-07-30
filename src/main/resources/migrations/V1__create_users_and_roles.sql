CREATE SCHEMA IF NOT EXISTS USER_SCHEMA;
CREATE SCHEMA IF NOT EXISTS ADDRESS_SCHEMA;

CREATE TABLE IF NOT EXISTS USER_SCHEMA.users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);
INSERT INTO USER_SCHEMA.users (id, name, email, password) VALUES
  ('1', 'Alice Smith', 'alice@example.com', 'password1'),
  ('2', 'Bob Johnson', 'bob@example.com', 'password2');

CREATE TABLE ADDRESS_SCHEMA.addresses (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255)
);
INSERT INTO ADDRESS_SCHEMA.addresses (id, name) VALUES
  ('1', 'Istanbul'),
  ('2', 'Ankara');

commit;
