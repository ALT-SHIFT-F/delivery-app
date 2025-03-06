package com.example.deliveryapp.order.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OrderStatus {
    ORDER_REQUESTED(0, "주문요청"),
    ORDER_ACCEPTED(1, "주문수락"),
    ORDER_REJECTED(2, "주문거절"),
    ORDER_CANCELED(3, "주문취소"),
    DELIVERING(4, "배달중"),
    DELIVERED(5, "배달완료");


    private final Integer code;
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatus of(Integer code) {
        return Arrays.stream(OrderStatus.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("데이터가 없습니다."));
    }
    // 배달완료 상태 확인
    public boolean isReviewable() {
        return this == DELIVERED;
    }
}
