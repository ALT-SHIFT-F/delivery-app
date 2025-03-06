package com.example.deliveryapp.order.entity;

import com.example.deliveryapp.common.entity.BaseEntity;
import com.example.deliveryapp.menu.entity.Menu;
import com.example.deliveryapp.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "order_detail")
public class OrderDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Long quantity;

    private BigDecimal price;

    public OrderDetail(Menu menu, Order order, Long quantity, BigDecimal price) {
        this.menu = menu;
        this.order = order;
        this.quantity = quantity;
        this.price = price;
    }
    public Store getStore() {
        return menu.getStore();
    }
}
