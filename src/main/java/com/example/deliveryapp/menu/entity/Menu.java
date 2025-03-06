package com.example.deliveryapp.menu.entity;

import com.example.deliveryapp.common.entity.BaseEntity;
import com.example.deliveryapp.menu.enums.MenuCategory;
import com.example.deliveryapp.menu.enums.MenuStatus;
import com.example.deliveryapp.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.example.deliveryapp.menu.enums.MenuStatus.*;

@Entity
@Table(name = "menu")
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String menuName;

    private BigDecimal price;

    @Enumerated
    private MenuCategory menuCategory;

    @Enumerated(EnumType.STRING)
    private MenuStatus menuStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="store_id")
    private Store store;

    private Integer salesCount;

    private Integer stockQuantity;

    private String description;

    @PrePersist
    public void PrePersist() {
        this.salesCount = salesCount == null ? 0 : this.salesCount; // 판매량 기본값 0, 처음 저장시 0
        this.menuStatus = menuStatus == null ? INACTIVE : menuStatus; // 메뉴 상태 기본값 INACTIVE
    }

    @Builder
    public Menu(String menuName, BigDecimal price, MenuCategory menuCategory,
                MenuStatus menuStatus, Integer stockQuantity, String description) {
        this.menuName = menuName;
        this.price = price;
        this.menuCategory = menuCategory;
        this.menuStatus = menuStatus;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    public void simpleUpdate(MenuStatus menuStatus, Integer stockQuantity) {
        if (menuStatus != null && !menuStatus.equals(this.menuStatus)) {
            this.menuStatus = menuStatus;
        }
        if (stockQuantity != null && !stockQuantity.equals(this.stockQuantity)) {
            this.stockQuantity = stockQuantity;
        }
    }

    public void update(String menuName, BigDecimal price, MenuCategory menuCategory,
                       MenuStatus menuStatus, Integer stockQuantity, String description) {
        this.menuName = menuName;
        this.price = price;
        this.menuCategory = menuCategory;
        this.menuStatus = menuStatus;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    public void delete() {
        this.menuStatus = DELETED;
    }

    public void updateStockQuantity(Integer stockQuantity){
        this.stockQuantity = stockQuantity;
    }

}
