package com.example.deliveryapp.review.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewSaveResponseDto {

    private final Long id;
    private final Long userId;
    private final Long storeId;
    private final Long orderId;
    private final String content;
    private final int score;
    private final LocalDateTime createdAt;

    public ReviewSaveResponseDto(Long id,
                                 Long userId,
                                 Long storeId,
                                 Long orderId,
                                 String content,
                                 int score,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.storeId = storeId;
        this.orderId = orderId;
        this.content = content;
        this.score = score;
        this.createdAt = createdAt;
    }
}
