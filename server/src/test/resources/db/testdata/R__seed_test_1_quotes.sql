-- Quotes for Alpha (company_id=2)
INSERT INTO quotes (
    id, company_id, customer_id,
    quote_year, quote_number, status,
    title, notes, issue_date, valid_until, currency,
    subtotal_amount, tax_amount, total_amount
) VALUES
    (1000, 2, 100, 2025, 10, 'DRAFT', 'Preventivo Base', 'Note base', DATE '2025-01-05', DATE '2025-02-05', 'EUR', 100.00, 22.00, 122.00),
    (1001, 2, 100, 2025, 11, 'SENT', 'Preventivo Inviato', 'Note invio', DATE '2025-01-10', DATE '2025-02-10', 'EUR', 200.00, 44.00, 244.00),
    (1002, 2, 101, 2025, 12, 'ACCEPTED', 'Preventivo Accettato', 'Note accettato', DATE '2025-01-15', DATE '2025-02-15', 'EUR', 150.00, 33.00, 183.00);

-- Quotes for Beta (company_id=3)
INSERT INTO quotes (
    id, company_id, customer_id,
    quote_year, quote_number, status,
    title, notes, issue_date, valid_until, currency,
    subtotal_amount, tax_amount, total_amount
) VALUES
    (1010, 3, 200, 2025, 5, 'DRAFT', 'Preventivo Beta', 'Note beta', DATE '2025-01-08', DATE '2025-02-08', 'EUR', 80.00, 17.60, 97.60),
    (1011, 3, 200, 2025, 6, 'SENT', 'Preventivo Beta Inviato', 'Note invio beta', DATE '2025-01-12', DATE '2025-02-12', 'EUR', 120.00, 26.40, 146.40),
    (1012, 3, 200, 2025, 7, 'ACCEPTED', 'Preventivo Beta Accettato', 'Note accettato beta', DATE '2025-01-18', DATE '2025-02-18', 'EUR', 90.00, 19.80, 109.80);

-- Quote items for 1010
INSERT INTO quote_items (
    id, quote_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (1110, 1010, 1, 'Analisi Beta', NULL, 2, 'h', 20.00, 22.00, 'NONE', 0, 40.00, 8.80, 48.80),
    (1111, 1010, 2, 'Report Beta', NULL, 1, 'pz', 40.00, 22.00, 'NONE', 0, 40.00, 8.80, 48.80);

-- Quote items for 1011
INSERT INTO quote_items (
    id, quote_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (1112, 1011, 1, 'Consulenza Beta', NULL, 3, 'h', 40.00, 22.00, 'NONE', 0, 120.00, 26.40, 146.40);

-- Quote items for 1012 (discount applied)
INSERT INTO quote_items (
    id, quote_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (1113, 1012, 1, 'Pacchetto Beta', 'Sconto 10%', 2, 'pz', 50.00, 22.00, 'PERCENT', 10, 90.00, 19.80, 109.80);

-- Quote items for 1000
INSERT INTO quote_items (
    id, quote_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (1100, 1000, 1, 'Analisi', NULL, 2, 'h', 25.00, 22.00, 'NONE', 0, 50.00, 11.00, 61.00),
    (1101, 1000, 2, 'Sviluppo', NULL, 1, 'h', 50.00, 22.00, 'NONE', 0, 50.00, 11.00, 61.00);

-- Quote items for 1001
INSERT INTO quote_items (
    id, quote_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (1102, 1001, 1, 'Consulenza', NULL, 4, 'h', 50.00, 22.00, 'NONE', 0, 200.00, 44.00, 244.00);

-- Quote items for 1002 (discount applied)
INSERT INTO quote_items (
    id, quote_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (1103, 1002, 1, 'Pacchetto', 'Sconto 10%', 3, 'pz', 50.00, 22.00, 'PERCENT', 10, 135.00, 29.70, 164.70);
