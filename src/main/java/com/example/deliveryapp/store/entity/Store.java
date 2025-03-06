package com.example.deliveryapp.store.entity;

import com.example.deliveryapp.common.entity.BaseEntity;
import com.example.deliveryapp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "store")
@Builder
public class Store extends BaseEntity {  // BaseEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessName;

    private String category;

    private LocalTime openingTime;

    private LocalTime closingTime;


    private Double minOrderPrice;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;  // 가게의 주인(사장님)

    private String address;

    @Builder
    public Store(String businessName, String category, LocalTime openingTime,
                 LocalTime closingTime, Double minOrderPrice, User owner) {
        this.businessName = businessName;
        this.category = category;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.minOrderPrice = minOrderPrice;
        this.owner = owner;
    }
}