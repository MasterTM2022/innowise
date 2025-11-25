package com.innowise.UserService.repository;

import com.innowise.UserService.entity.Card;
import org.springframework.boot.web.server.ErrorPageRegistrarBeanPostProcessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // Named Methods
    Page<Card> findByUserId(Long userId, Pageable pageable);
    Optional<Card> findById(Long id);
    void deleteById(Long id);

    // Pagination for Cards
    Page<Card> findAll(Pageable pageable);

    // JPQL
    @Query("SELECT c FROM Card c WHERE c.expirationDate < :date")
    Page<Card> findExpiredCards(@Param("date") LocalDate date, Pageable pageable);

    // Native SQL
    @Query(value = "SELECT * FROM card_info WHERE number LIKE CONCAT('%', :fourDigits, '%')",
            nativeQuery = true)
    Page<Card> findByLast4Digits(@Param("fourDigits") String fourDigits, Pageable pageable);
}