package com.innowise.UserService.repository;

import com.innowise.UserService.entity.Order;
import com.innowise.UserService.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // 1. Найти заказы по ID пользователей (для получения чужих заказов или своих)
    List<Order> findByUserId(Long userId);

    // 2. Найти заказы по ID (для batch-запросов)
    List<Order> findByIdIn(List<Long> ids);

    // 3. Найти заказы по статусам (для фильтрации)
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    // 4. Найти заказы по ID пользователей с пагинацией
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // 5. Найти заказы по статусам с пагинацией
    Page<Order> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);

    // 6. Найти заказы по ID пользователей и статусам
    List<Order> findByUserIdAndStatusIn(Long userId, List<OrderStatus> statuses);

    // 7. Найти заказы по ID пользователей и статусам с пагинацией
    Page<Order> findByUserIdAndStatusIn(Long userId, List<OrderStatus> statuses, Pageable pageable);

    // 8. Найти заказы с датой создания до определённой (например, истёкшие)
    @Query("SELECT o FROM Order o WHERE o.creationDate < :date")
    List<Order> findOrdersCreatedBefore(@Param("date") LocalDateTime date);

    // 9. Найти заказы с датой создания до определённой с пагинацией
    @Query("SELECT o FROM Order o WHERE o.creationDate < :date")
    Page<Order> findOrdersCreatedBefore(@Param("date") LocalDateTime date, Pageable pageable);

    // 10. Найти заказы, у которых пользователь в списке (для админских запросов)
    @Query("SELECT o FROM Order o WHERE o.userId IN :userIds")
    List<Order> findByUserIds(@Param("userIds") List<Long> userIds);

    // 11. Найти заказы, у которых пользователь в списке, с пагинацией
    @Query("SELECT o FROM Order o WHERE o.userId IN :userIds")
    Page<Order> findByUserIds(@Param("userIds") List<Long> userIds, Pageable pageable);

    // 12. Проверить, является ли пользователь владельцем заказа
    // Используется в сервисе для проверки @PreAuthorize
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.id = :orderId AND o.userId = :userId")
    boolean existsByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    // 13. Удалить заказы пользователя (для очистки)
    void deleteByUserId(Long userId);

    // 14. Удалить заказы по статусу
    void deleteByStatus(OrderStatus status);

}