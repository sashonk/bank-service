package ru.asocial.learn.day2.dao.billing;

import java.math.BigDecimal;

public interface PostingDAO {

    /**
     * Баланс счёта, вычисленный из проводок: сумма credit − сумма debit (issue #3).
     * Для счёта без проводок вернёт 0.
     */
    BigDecimal getAccountBalance(long accountId);

}
