package com.example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashCardRepository extends JpaRepository<CashCard, Long> {
    // SELECT * FROM cash_card WHERE id = ? AND owner = ?;
    CashCard findByIdAndOwner(Long id, String owner);
    boolean existsByIdAndOwner(Long id, String owner);

    Page<CashCard> findByOwner(String owner, Pageable pageable);

}