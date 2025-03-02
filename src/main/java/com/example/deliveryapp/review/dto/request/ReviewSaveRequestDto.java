package com.example.deliveryapp.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReviewSaveRequestDto {

    private Long id;
    private Long storeId;
    private Long orderId;

    @NotBlank(message = "내용을 입력하세요.")
    private String content;


    @NotBlank(message = "별점을 선택하세요.")
    private int score;


}
