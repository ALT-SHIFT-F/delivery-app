package com.example.deliveryapp.cart.dto.request;

import lombok.Getter;

@Getter
public class CartSaveRequestDto {
    private Long memberId;
    private Long quantity;
    private Long menuId;
}
