package ru.asocial.learn.day2.dao.billing;

import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.util.List;

public interface PostingDAO {

    /**
     * Баланс счёта, вычисленный из проводок: сумма credit − сумма debit (issue #3).
     * Для счёта без проводок вернёт 0.
     */
    BigDecimal getAccountBalance(long accountId);

    /**
     * Проводки, затрагивающие счёт (дебетовая или кредитовая сторона),
     * новые сверху (issue #6).
     */
    List<Posting> findByAccount(long accountId);

}
