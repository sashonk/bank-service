package ru.asocial.learn.day2.model.billing;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import ru.asocial.learn.day2.model.Posting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BillingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Instant dateTimeCreated;

    @Column
    private String description;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Posting> postings = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(Instant dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public void addPosting(Posting posting) {
        this.postings.add(posting);
        posting.setTransaction(this);
    }

    public void removePosting(Posting posting) {
        this.postings.remove(posting);
        posting.setTransaction(null);
    }
}
