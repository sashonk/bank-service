package ru.asocial.learn.day2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Client extends Party {

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String externalId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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
}
