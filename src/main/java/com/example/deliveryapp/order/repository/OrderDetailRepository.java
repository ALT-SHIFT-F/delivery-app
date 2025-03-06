package com.example.deliveryapp.order.repository;

import com.example.deliveryapp.order.entity.OrderDetail;
import com.example.deliveryapp.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT d FROM OrderDetail d " +
            "LEFT JOIN FETCH d.menu m " +
            "WHERE d.order.id = :orderId")
    List<OrderDetail> findByOrderId(@Param("orderId") Long orderId);

    @Modifying
    @Query("DELETE FROM OrderDetail od WHERE od.menu.store = :store")
    void deleteByMenuStore(@Param("store") Store store);
}
