INSERT INTO companies (name, vat_number)
VALUES ('__PLATFORM__', NULL);

INSERT INTO users (company_id, name, email, password_hash, role, enabled)
VALUES (
           (SELECT id FROM companies WHERE name='__PLATFORM__'),
           'Giuseppe Garibaldi',
           'platform.admin@easyinvoice.com',
           '$2a$12$3zC7J6G1IJQcrCQn9OlFNu9sUhlSlbaGtZgHAnDYvazbGOBUYtyy.',
           'PLATFORM_ADMIN',
           TRUE
       );

-- Admin123!!