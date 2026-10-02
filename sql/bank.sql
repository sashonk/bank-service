-- Банк «Банк Москвы» (БИК 044525219): посевные данные, скрипт выполняется вручную.
--
-- ЧАСТЬ 1 — выполнять ТОЛЬКО на чистой БД: party-строка банка вставляется безусловно,
-- и на БД, где банк уже заведён, повтор даст дубликат. Проверка: select * from bank where code = '044525219'.
--
-- ЧАСТЬ 2 — идемпотентна: каждая вставка счёта защищена NOT EXISTS по account_number,
-- повторный запуск на любой БД дубликатов не создаст.

-- ============ ЧАСТЬ 1: банк (только чистая БД) ============

-- Банк: строка в party + строка в bank (JOINED-наследование).
-- code — БИК банка (Bank.code).
INSERT INTO party (id) VALUES (DEFAULT);

INSERT INTO bank (id, name, code)
SELECT MAX(id), 'АО «Банк Москвы»', '044525219' FROM party;

-- ============ ЧАСТЬ 2: счета банка (идемпотентно) ============

-- Касса банка (CASH_DESK): RUB и USD.
INSERT INTO billing_account (account_number, currency_id, party_id, type)
SELECT '20202810000000000001', c.id, b.id, 'CASH_DESK'
FROM currency c, bank b
WHERE c.code = 'RUB' AND b.code = '044525219'
  AND NOT EXISTS (SELECT 1 FROM billing_account ba WHERE ba.account_number = '20202810000000000001');

INSERT INTO billing_account (account_number, currency_id, party_id, type)
SELECT '20202840000000000002', c.id, b.id, 'CASH_DESK'
FROM currency c, bank b
WHERE c.code = 'USD' AND b.code = '044525219'
  AND NOT EXISTS (SELECT 1 FROM billing_account ba WHERE ba.account_number = '20202840000000000002');

-- Комиссионные счета (COMMISSION): RUB и USD.
INSERT INTO billing_account (account_number, currency_id, party_id, type)
SELECT '47401810000000000003', c.id, b.id, 'COMMISSION'
FROM currency c, bank b
WHERE c.code = 'RUB' AND b.code = '044525219'
  AND NOT EXISTS (SELECT 1 FROM billing_account ba WHERE ba.account_number = '47401810000000000003');

INSERT INTO billing_account (account_number, currency_id, party_id, type)
SELECT '47401840000000000004', c.id, b.id, 'COMMISSION'
FROM currency c, bank b
WHERE c.code = 'USD' AND b.code = '044525219'
  AND NOT EXISTS (SELECT 1 FROM billing_account ba WHERE ba.account_number = '47401840000000000004');
