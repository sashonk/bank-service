-- Банк: строка в party + строка в bank (JOINED-наследование).
-- code — БИК банка (Bank.code).
INSERT INTO party (id) VALUES (DEFAULT);

INSERT INTO bank (id, name, code)
SELECT MAX(id), 'АО «Банк Москвы»', '044525219' FROM party;

-- Корсчет банка в рублях.
INSERT INTO billing_account (account_number, currency_id, party_id)
SELECT '30101810400000000225', c.id, (SELECT MAX(id) FROM party)
FROM currency c WHERE c.code = 'RUB';

-- Счета банка в USD и EUR; банк ищется по коду, валюты — из справочника currency.
INSERT INTO billing_account (account_number, currency_id, party_id)
SELECT '30101810900000000123', c.id, b.id
FROM currency c, bank b
WHERE c.code = 'USD' AND b.code = '044525219';

INSERT INTO billing_account (account_number, currency_id, party_id)
SELECT '30101810800000000456', c.id, b.id
FROM currency c, bank b
WHERE c.code = 'EUR' AND b.code = '044525219';
