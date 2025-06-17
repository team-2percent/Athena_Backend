package goorm.athena.domain.order.repository;


import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderRepository extends JpaRepository<Order, Long> {
//    @Query(value = """
//            SELECT u.*
//            FROM orders o
//            JOIN project p ON o.project_id = p.id
//            JOIN `user` u ON p.seller_id = u.id
//            WHERE o.id = :orderId
//            """, nativeQuery = true)
//    User findSellerByOrderId(@Param("orderId") Long orderId);
//
//    @Query(value = """
//            SELECT u.*
//            FROM orders o
//            JOIN `user` u ON o.user_id = u.id
//            WHERE o.id = :orderId
//            """, nativeQuery = true)
//    User findBuyerByOrderId(@Param("orderId") Long orderId);
}