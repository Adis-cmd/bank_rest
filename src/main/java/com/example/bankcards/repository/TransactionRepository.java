package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Modifying
    @Transactional
    @Query("delete from Transaction t WHERE t.fromCard.id = :fromCardId or t.toCard.id = :toCardId")
    void deleteByFromCardIdOrToCardId(
            @Param("fromCardId") Long fromCardId,
            @Param("toCardId") Long toCardId);

}
