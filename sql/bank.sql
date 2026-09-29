INSERT INTO party (id) VALUES (DEFAULT);

INSERT INTO bank (id, name)
SELECT MAX(id), 'АО «Банк Москвы»' FROM party;

INSERT INTO billing_account (account_number, currency_id, party_id)
SELECT '30101810400000000225', c.id, (SELECT MAX(id) FROM party)
FROM currency c WHERE c.code = 'RUB';

-- Счета банка в USD и EUR.
-- Банк ищется по имени (создан ранее скриптом с id = 5001, «АО «Банк Москвы»»),
-- валюты — по коду из справочника currency.

INSERT INTO billing_account (account_number, currency_id, party_id)
SELECT '30101810900000000123', c.id, b.id
FROM currency c, bank b
WHERE c.code = 'USD' AND b.name = 'АО «Банк Москвы»';

INSERT INTO billing_account (account_number, currency_id, party_id)
SELECT '30101810800000000456', c.id, b.id
FROM currency c, bank b
WHERE c.code = 'EUR' AND b.name = 'АО «Банк Москвы»';