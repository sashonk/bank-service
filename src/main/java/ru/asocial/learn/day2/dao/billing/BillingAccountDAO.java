package ru.asocial.learn.day2.dao.billing;

import ru.asocial.learn.day2.model.billing.BillingAccount;

public interface BillingAccountDAO {

    BillingAccount getById(long id);

}
