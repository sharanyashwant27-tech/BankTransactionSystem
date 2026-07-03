package com.bank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    public static final String TYPE_DEBIT = "DEBIT";
    public static final String TYPE_CREDIT = "CREDIT";

    @Id
    @GeneratedValue
    private Long id;

    private String description;

    private Double amount;

    private LocalDate transactionDate;

    /** DEBIT reduces balance; CREDIT increases balance. Null treated as DEBIT. */
    private String type;

    @ManyToOne
    @JsonIgnore
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCredit() {
        return TYPE_CREDIT.equalsIgnoreCase(type);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
