package com.example.cashcard;

import jakarta.persistence.*;


import java.util.Objects;

@Entity
@Table(name = "cashCard")
public class CashCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String owner;

    public CashCard(Long id, Double amount, String owner) {
        this.id = id;
        this.amount = amount;
        this.owner = owner;
    }

    public CashCard() {
        
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CashCard cashCard = (CashCard) o;
        return Objects.equals(id, cashCard.id) && Objects.equals(amount, cashCard.amount) && Objects.equals(owner, cashCard.owner);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, amount, owner);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
