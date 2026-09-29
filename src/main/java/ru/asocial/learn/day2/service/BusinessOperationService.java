package ru.asocial.learn.day2.service;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.asocial.learn.day2.dao.BusinessOperationDAO;
import ru.asocial.learn.day2.dto.BusinessOperationDTO;
import ru.asocial.learn.day2.dto.CreateBusinessOperationDTO;
import ru.asocial.learn.day2.mapper.BusinessOperationMapper;
import ru.asocial.learn.day2.model.BusinessOperation;
import ru.asocial.learn.day2.model.Client;

@Service
public class BusinessOperationService {

    @Autowired
    private ClientService clientService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private CardPaymentService cardPaymentService;
    @Autowired
    private CashWithdrawalService cashWithdrawalService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private BusinessOperationDAO businessOperationDAO;
    @Autowired
    private BusinessOperationMapper businessOperationMapper;

    @Transactional
    public BusinessOperationDTO createAndProcess(@Nonnull CreateBusinessOperationDTO businessOperationDTO) {
        BusinessOperation businessOperation = new BusinessOperation();
        Client client = clientService.getClientOrThrow(businessOperationDTO.getClientId());
        businessOperation.setClient(client);
        businessOperation.setAccountNumber(businessOperationDTO.getAccountNumber());
        businessOperation.setAmount(businessOperationDTO.getAmount());
        businessOperation.setOperationType(BusinessOperation.BusinessOperationType.valueOf(businessOperationDTO.getOperationType()));
        if (businessOperationDTO.getAccountNumber2() != null) {
            businessOperation.setAccountNumber2(businessOperationDTO.getAccountNumber2());
        }
        if (businessOperationDTO.getClientId2() != null) {
            Client client2 = clientService.getClientOrThrow(businessOperationDTO.getClientId2());
            businessOperation.setClient2(client2);
        }
        businessOperation = businessOperationDAO.save(businessOperation);

        switch (businessOperation.getOperationType()) {
            case DEPOSIT -> depositService.processDeposit(businessOperation);
            case CARD_PAYMENT -> cardPaymentService.processCardPayment(businessOperation);
            case TRANSFER -> transferService.processTransfer(businessOperation);
            case CASH_WITHDRAWAL -> cashWithdrawalService.processCashWithdrawal(businessOperation);
            default -> throw new IllegalArgumentException("Unexpected operation type: " + businessOperationDTO.getOperationType());
        }

        return businessOperationMapper.map(businessOperation) ;
    }

}
