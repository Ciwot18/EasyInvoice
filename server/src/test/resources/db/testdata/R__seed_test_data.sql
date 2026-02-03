-- Companies (id=1 reserved for __PLATFORM__ from V2 migration)
INSERT INTO companies (id, name, vat_number) VALUES (2, 'Alpha SRL', 'IT000000001');
INSERT INTO companies (id, name, vat_number) VALUES (3, 'Beta SPA', 'IT000000002');

-- Users for Platform (company_id=1)
INSERT INTO users (id, company_id, email, password_hash, role, enabled)
VALUES (13, 1, 'admin@alpha.it', '$2a$12$dummyhash3333333333333333333333333333333333333333333333', 'PLATFORM_ADMIN', TRUE);

-- Users for Alpha (company_id=2)
INSERT INTO users (id, company_id, email, password_hash, role, enabled)
VALUES (10, 2, 'manager@alpha.it', '$2a$12$dummyhash0000000000000000000000000000000000000000000000', 'COMPANY_MANAGER', TRUE);
INSERT INTO users (id, company_id, email, password_hash, role, enabled)
VALUES (11, 2, 'backoffice@alpha.it', '$2a$12$dummyhash1111111111111111111111111111111111111111111111', 'BACK_OFFICE', TRUE);
INSERT INTO users (id, company_id, email, password_hash, role, enabled)
VALUES (12, 2, 'zeta@alpha.it', '$2a$12$dummyhash2222222222222222222222222222222222222222222222', 'BACK_OFFICE', FALSE);

-- Users for Beta (company_id=3)
INSERT INTO users (id, company_id, email, password_hash, role, enabled)
VALUES (20, 3, 'manager@beta.it', '$2a$12$dummyhash4444444444444444444444444444444444444444444444', 'COMPANY_MANAGER', TRUE);
INSERT INTO users (id, company_id, email, password_hash, role, enabled)
VALUES (21, 3, 'backoffice@beta.it', '$2a$12$dummyhash5555555555555555555555555555555555555555555555', 'BACK_OFFICE', TRUE);

-- Customers for Alpha (company_id=2)
INSERT INTO customers (
    id, company_id, display_name, legal_name, status,
    email, phone, vat_number, pec,
    address, city, postal_code, country
) VALUES (
             100, 2, 'Alfa Uno', 'Alfa Uno SRL', 'ACTIVE',
             'info@alfa1.it', '123456', 'ITVAT001', 'pec@alfa1.it',
             'Via Roma 1', 'Roma', '00100', 'IT'
         );

INSERT INTO customers (
    id, company_id, display_name, legal_name, status,
    email, phone, vat_number, pec,
    address, city, postal_code, country
) VALUES (
             101, 2, 'Beta Due', 'Beta Due SRL', 'ACTIVE',
             'contact@betadue.it', '654321', 'ITVAT002', 'pec@betadue.it',
             'Via Milano 2', 'Milano', '20100', 'IT'
         );

INSERT INTO customers (
    id, company_id, display_name, legal_name, status,
    email, phone, vat_number, pec,
    address, city, postal_code, country
) VALUES (
             102, 2, 'Gamma Tre', 'Gamma Tre SRL', 'ARCHIVED',
             'arch@gamma.it', '777777', 'ITVAT003', 'pec@gamma.it',
             'Via Torino 3', 'Torino', '10100', 'IT'
         );

INSERT INTO customers (
    id, company_id, display_name, legal_name, status,
    email, phone, vat_number, pec,
    address, city, postal_code, country
) VALUES (
             103, 2, 'Delta Quattro', NULL, 'DELETED',
             NULL, NULL, 'ITVAT004', NULL,
             'Rue de Paris 4', 'Paris', '75000', 'FR'
         );

-- Customers for Beta (company_id=3)
INSERT INTO customers (
    id, company_id, display_name, legal_name, status,
    email, phone, vat_number, pec,
    address, city, postal_code, country
) VALUES (
             200, 3, 'Omega Uno', 'Omega Uno SPA', 'ACTIVE',
             'info@omega.it', '888888', 'BTVAT001', 'pec@omega.it',
             'Via Napoli 5', 'Napoli', '80100', 'IT'
         );

INSERT INTO customers (
    id, company_id, display_name, legal_name, status,
    email, phone, vat_number, pec,
    address, city, postal_code, country
) VALUES (
             201, 3, 'Sigma Due', 'Sigma Due SPA', 'ARCHIVED',
             'info@sigma.it', '999999', 'BTVAT002', 'pec@sigma.it',
             'Via Bari 6', 'Bari', '70100', 'IT'
         );