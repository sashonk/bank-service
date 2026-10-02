package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.asocial.learn.day2.dao.ClientDao;
import ru.asocial.learn.day2.dao.billing.BillingAccountDAO;
import ru.asocial.learn.day2.dao.billing.PostingDAO;
import ru.asocial.learn.day2.dto.billing.AccountHistoryEntryDTO;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.mapper.AccountHistoryMapper;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.Posting;

import java.util.ArrayList;
import java.util.List;

/**
 * История транзакций по счёту клиента (issue #6): транзакция попадает в историю,
 * если хотя бы одна её проводка затрагивает счёт.
 */
@Service
public class AccountHistoryService {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private BillingAccountDAO billingAccountDAO;

    @Autowired
    private PostingDAO postingDAO;

    @Autowired
    private AccountHistoryMapper accountHistoryMapper;

    @Transactional(readOnly = true)
    public List<AccountHistoryEntryDTO> getAccountHistory(Long clientId, Long accountId) {
        Client client = clientDao.getById(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("client not found, id = " + clientId);
        }

        BillingAccount account = billingAccountDAO.getById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("account not found, id = " + accountId);
        }
        // Счёт существует, но принадлежит другому участнику — это ошибка бизнес-валидации, а не «не найдено».
        if (account.getParty() == null || !clientId.equals(account.getParty().getId())) {
            throw new BusinessValidationException(
                    "Billing account " + accountId + " does not belong to client " + clientId);
        }

        List<Posting> postings = postingDAO.findByAccount(accountId);
        List<AccountHistoryEntryDTO> history = new ArrayList<>(postings.size());
        for (Posting posting : postings) {
            history.add(accountHistoryMapper.map(posting, accountId));
        }
        return history;
    }
}
