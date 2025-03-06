package com.example.deliveryapp.review.entity;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.common.entity.BaseEntity;
import com.example.deliveryapp.order.entity.Order;
import com.example.deliveryapp.store.entity.Store;
import com.example.deliveryapp.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private String content;

    @Builder
    public Review(User user,
            Store store,
                  Order order,
                  int score,
                  String content) {
        this.user = user;
        this.store = store;
        this.order = order;
        this.score = score;
        this.content = content;
    }

    public void update(String content, int score) {
        this.content = content;
        this.score = score;
    }
}
