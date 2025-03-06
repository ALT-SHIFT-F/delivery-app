package com.example.deliveryapp.menu.dto.response;

import com.example.deliveryapp.menu.entity.Menu;
import com.example.deliveryapp.menu.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class MenuSimpleResponseDto {

    private final Long id;
    private final String menuName;
    private final BigDecimal price;
    private final MenuCategory menuCategory;
    private final Integer stockQunatity;
    private final String description;

    public static MenuSimpleResponseDto toDto(Menu menu) {
        return MenuSimpleResponseDto.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .price(menu.getPrice())
                .menuCategory(menu.getMenuCategory())
                .stockQunatity(menu.getStockQuantity())
                .description(menu.getDescription())
                .build();
    }
}
