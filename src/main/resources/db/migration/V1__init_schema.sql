-- V1: Initial schema for Highway Helper

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(150)  NOT NULL,
    phone_number    VARCHAR(20)   NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    password        VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone_number),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'MECHANIC', 'ADMIN'))
);

CREATE TABLE mechanics (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT         NOT NULL,
    shop_name               VARCHAR(200)   NOT NULL,
    owner_name              VARCHAR(150)   NOT NULL,
    phone_number            VARCHAR(20)    NOT NULL,
    alternate_phone_number  VARCHAR(20)    NULL,
    address                 VARCHAR(500)   NOT NULL,
    city                    VARCHAR(100)   NOT NULL,
    state                   VARCHAR(100)   NOT NULL,
    latitude                DECIMAL(10, 7) NULL,
    longitude               DECIMAL(10, 7) NULL,
    services_offered        VARCHAR(1000)  NOT NULL,
    operating_hours         VARCHAR(500)   NULL,
    years_of_experience     INT            NULL,
    is_verified             BOOLEAN        NOT NULL DEFAULT FALSE,
    status                  VARCHAR(20)    NOT NULL DEFAULT 'INACTIVE',
    created_at              DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_mechanics_user_id UNIQUE (user_id),
    CONSTRAINT fk_mechanics_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_mechanics_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_mechanics_city ON mechanics (city);
CREATE INDEX idx_mechanics_status ON mechanics (status);

CREATE TABLE service_requests (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    mechanic_id     BIGINT         NULL,
    vehicle_type    VARCHAR(50)    NOT NULL,
    vehicle_number  VARCHAR(20)    NOT NULL,
    issue_type      VARCHAR(100)   NOT NULL,
    description     VARCHAR(1000)  NOT NULL,
    latitude        DECIMAL(10, 7) NOT NULL,
    longitude       DECIMAL(10, 7) NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    requested_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at    DATETIME(6)    NULL,
    CONSTRAINT fk_service_requests_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_service_requests_mechanic FOREIGN KEY (mechanic_id) REFERENCES mechanics (id),
    CONSTRAINT chk_service_requests_status CHECK (status IN ('PENDING', 'ACCEPTED', 'ON_THE_WAY', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_service_requests_user_id ON service_requests (user_id);
CREATE INDEX idx_service_requests_mechanic_id ON service_requests (mechanic_id);
CREATE INDEX idx_service_requests_status ON service_requests (status);
