package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Boolean existsByCardNumber(String cardNumber);


    @Query("select c from Card c where c.owner.id = :userId")
    Page<Card> findAllByOwnerId(@Param("userId") Long userId, Pageable pageable);

    @Query("select c from Card c where c.owner.id = :userId and c.status = :status")
    Page<Card> findByOwnerIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status, Pageable pageable);
}
