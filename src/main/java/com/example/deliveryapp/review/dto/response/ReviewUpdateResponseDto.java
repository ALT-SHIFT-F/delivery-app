package com.example.deliveryapp.review.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewUpdateResponseDto {

    private final Long id;
    private final Long storeId;
    private final int score;
    private final String content;
    private final LocalDateTime updatedAt;

    public ReviewUpdateResponseDto(Long id,
                                   Long storeId,
                                   int score,
                                   String content,
                                   LocalDateTime updatedAt) {
        this.id = id;
        this.storeId = storeId;
        this.score = score;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}
