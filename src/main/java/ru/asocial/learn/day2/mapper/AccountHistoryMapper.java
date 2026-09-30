package ru.asocial.learn.day2.mapper;

import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.billing.AccountHistoryEntryDTO;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;

@Component
public class AccountHistoryMapper {

    /**
     * Проводка → запись истории по счёту accountId.
     * Счёт в кредите проводки — входящая операция (+), в дебете — исходящая (−).
     */
    public AccountHistoryEntryDTO map(Posting posting, long accountId) {
        AccountHistoryEntryDTO dto = new AccountHistoryEntryDTO();
        dto.setDateTime(posting.getTransaction().getDateTimeCreated());
        dto.setDescription(posting.getTransaction().getDescription());

        boolean incoming = posting.getCreditAccount() != null
                && posting.getCreditAccount().getId() != null
                && posting.getCreditAccount().getId() == accountId;
        BigDecimal amount = posting.getAmount() != null ? posting.getAmount() : BigDecimal.ZERO;
        dto.setAmount(incoming ? amount : amount.negate());

        BillingAccount clientSide = incoming ? posting.getCreditAccount() : posting.getDebitAccount();
        dto.setCurrency(clientSide != null && clientSide.getCurrency() != null
                ? clientSide.getCurrency().getCode() : null);
        return dto;
    }
}
