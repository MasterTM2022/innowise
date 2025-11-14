package com.innowise.UserService.repository;

import com.innowise.UserService.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Named Query Methods (Spring Data JPA)
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    void deleteById(Long id);

    // Pagination
    Page<User> findAll(Pageable pageable);

    // JPQL
    @Query("SELECT u FROM User u WHERE u.surname = :surname")
    List<User> findBySurname(@Param("surname") String surname);

    // Native SQL
    @Query(value = "SELECT * FROM users WHERE EXTRACT(YEAR FROM birth_date) = :year",
            nativeQuery = true)
    List<User> findByBirthYear(@Param("year") int year);

    Long id(Long id);
}
