-- Invoices for Alpha (company_id=2)
INSERT INTO invoices (
    id, company_id, customer_id, source_quote_id,
    invoice_year, invoice_number, status,
    title, notes, issue_date, due_date, currency,
    subtotal_amount, tax_amount, total_amount
) VALUES
    (2000, 2, 100, NULL, 2025, 20, 'DRAFT', 'Fattura Bozza', 'Note bozza', DATE '2025-01-20', NULL, 'EUR', 100.00, 22.00, 122.00),
    (2001, 2, 100, 1002, 2025, 21, 'ISSUED', 'Fattura Emessa', 'Da preventivo', DATE '2025-01-25', DATE '2025-02-25', 'EUR', 150.00, 33.00, 183.00),
    (2002, 2, 101, NULL, 2025, 22, 'PAID', 'Fattura Pagata', 'Saldo', DATE '2025-01-28', DATE '2025-02-28', 'EUR', 200.00, 44.00, 244.00);

-- Invoices for Beta (company_id=3)
INSERT INTO invoices (
    id, company_id, customer_id, source_quote_id,
    invoice_year, invoice_number, status,
    title, notes, issue_date, due_date, currency,
    subtotal_amount, tax_amount, total_amount
) VALUES
    (2010, 3, 200, NULL, 2025, 5, 'DRAFT', 'Fattura Beta Bozza', 'Note beta', DATE '2025-01-18', NULL, 'EUR', 80.00, 17.60, 97.60),
    (2011, 3, 200, 1012, 2025, 6, 'ISSUED', 'Fattura Beta Emessa', 'Da preventivo beta', DATE '2025-01-22', DATE '2025-02-22', 'EUR', 90.00, 19.80, 109.80),
    (2012, 3, 200, NULL, 2025, 7, 'PAID', 'Fattura Beta Pagata', 'Saldo beta', DATE '2025-01-26', DATE '2025-02-26', 'EUR', 120.00, 26.40, 146.40);

-- Invoice items for 2000
INSERT INTO invoice_items (
    id, invoice_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (2100, 2000, 1, 'Analisi', NULL, 2, 'h', 25.00, 22.00, 'NONE', 0, 50.00, 11.00, 61.00),
    (2101, 2000, 2, 'Sviluppo', NULL, 1, 'h', 50.00, 22.00, 'NONE', 0, 50.00, 11.00, 61.00);

-- Invoice items for 2001
INSERT INTO invoice_items (
    id, invoice_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (2102, 2001, 1, 'Pacchetto', 'Sconto 10%', 3, 'pz', 50.00, 22.00, 'PERCENT', 10, 135.00, 29.70, 164.70);

-- Invoice items for 2002
INSERT INTO invoice_items (
    id, invoice_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (2103, 2002, 1, 'Consulenza', NULL, 4, 'h', 50.00, 22.00, 'NONE', 0, 200.00, 44.00, 244.00);

-- Invoice items for 2010
INSERT INTO invoice_items (
    id, invoice_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (2110, 2010, 1, 'Analisi Beta', NULL, 2, 'h', 20.00, 22.00, 'NONE', 0, 40.00, 8.80, 48.80),
    (2111, 2010, 2, 'Report Beta', NULL, 1, 'pz', 40.00, 22.00, 'NONE', 0, 40.00, 8.80, 48.80);

-- Invoice items for 2011
INSERT INTO invoice_items (
    id, invoice_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (2112, 2011, 1, 'Pacchetto Beta', 'Sconto 10%', 2, 'pz', 50.00, 22.00, 'PERCENT', 10, 90.00, 19.80, 109.80);

-- Invoice items for 2012
INSERT INTO invoice_items (
    id, invoice_id, position,
    description, notes,
    quantity, unit, unit_price, tax_rate,
    discount_type, discount_value,
    line_subtotal_amount, line_tax_amount, line_total_amount
) VALUES
    (2113, 2012, 1, 'Consulenza Beta', NULL, 3, 'h', 40.00, 22.00, 'NONE', 0, 120.00, 26.40, 146.40);
