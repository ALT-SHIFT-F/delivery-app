package com.example.deliveryapp.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ReviewSaveRequestDto {
    @NotNull(message = "상점id는 필수 값입니다.")
    private Long storeId;
    @NotBlank(message = "내용을 입력하세요.")
    private String content;


    @NotNull(message = "별점을 선택하세요.")
    @Min(value = 1, message = "평점은 최소 1점 이상입니다.")
    @Max(value = 5, message = "평점은 최대 5점 이하입니다.")
    private int score;


}
