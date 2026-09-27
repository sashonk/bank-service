package ru.asocial.learn.day2.dto;

import java.math.BigDecimal;

public class BusinessOperationDTO {

    private String operationType;

    private BigDecimal amount;

    private Long clientId;

    private Long clientId2;

    private String accountNumber;

    private String accountNumber2;

    public Long getClientId() {
        return clientId;
    }

    public Long getClientId2() {
        return clientId2;
    }

    public void setClientId2(Long clientId2) {
        this.clientId2 = clientId2;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber2() {
        return accountNumber2;
    }

    public void setAccountNumber2(String accountNumber2) {
        this.accountNumber2 = accountNumber2;
    }
}
