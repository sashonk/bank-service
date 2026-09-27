package ru.asocial.learn.day2.dto.billing;

import java.time.Instant;
import java.util.List;

public class BillingTransactionDTO {

    private Long id;

    private Instant dateTimeCreated;

    private String description;

    private List<PostingDTO> postings;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PostingDTO> getPostings() {
        return postings;
    }

    public void setPostings(List<PostingDTO> postings) {
        this.postings = postings;
    }
}
