package ru.asocial.learn.day2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * Банк — участник отношений (например, контрагент по межбанковским операциям).
 */
@Entity
public class Bank extends Party {

    @Column
    private String code;

    @Column
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
