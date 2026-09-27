package ru.asocial.learn.day2.dto;

import ru.asocial.learn.day2.dto.billing.BillingAccountDTO;

import java.util.List;

public class CreateClientDTO {

    private String firstName;

    private String lastName;

    private String externalId;

    private List<BillingAccountDTO> accounts;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List<BillingAccountDTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccountDTO> accounts) {
        this.accounts = accounts;
    }
}
