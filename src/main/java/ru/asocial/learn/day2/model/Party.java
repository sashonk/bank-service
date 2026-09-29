package ru.asocial.learn.day2.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import ru.asocial.learn.day2.model.billing.BillingAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * Участник отношений (клиент, банк и т.д.). Владеет биллинг-счетами.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "party", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BillingAccount> accounts = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }

    public void addAccount(BillingAccount account) {
        this.accounts.add(account);
        account.setParty(this);
    }

    public void removeAccount(BillingAccount account) {
        this.accounts.remove(account);
        account.setParty(null);
    }
}
