package ru.asocial.learn.day2.dto;

import ru.asocial.learn.day2.dto.billing.BillingAccountDTO;

import java.util.List;

public class ClientDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private List<BillingAccountDTO> accounts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<BillingAccountDTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccountDTO> accounts) {
        this.accounts = accounts;
    }
}
