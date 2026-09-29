package ru.asocial.learn.day2.mapper;

import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.billing.BillingTransactionDTO;
import ru.asocial.learn.day2.dto.billing.PostingDTO;
import ru.asocial.learn.day2.model.billing.Posting;
import ru.asocial.learn.day2.model.billing.BillingTransaction;

import java.util.LinkedList;

@Component
public class BillingTransactionMapper {

    public BillingTransactionDTO map(BillingTransaction entity, boolean mapPostings) {
        BillingTransactionDTO dto = new BillingTransactionDTO();
        dto.setId(entity.getId());
        dto.setDateTimeCreated(entity.getDateTimeCreated());
        dto.setDescription(entity.getDescription());
        dto.setPostings(new LinkedList<>());
        if (mapPostings) {
            for (Posting posting : entity.getPostings()) {
                PostingDTO postingDTO = new PostingDTO();
                postingDTO.setId(posting.getId());
                postingDTO.setDebitAccount(posting.getDebitAccount().getAccountNumber());
                postingDTO.setCreditAccount(posting.getCreditAccount().getAccountNumber());
                postingDTO.setAmount(posting.getAmount());
                dto.getPostings().add(postingDTO);
            }
        }
        return dto;
    }
}
