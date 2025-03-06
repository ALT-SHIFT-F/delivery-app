package com.example.deliveryapp.review.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {

    private final Long id;
    private final Long userId;
    private final int score;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ReviewResponseDto(Long id,
                             Long userId,
                             int score,
                             String content,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.score = score;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
