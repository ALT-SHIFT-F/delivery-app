package com.example.deliveryapp.review.controller;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.common.annotation.Auth;
import com.example.deliveryapp.common.exception.custom_exception.CustomException;
import com.example.deliveryapp.common.exception.errorcode.ErrorCode;
import com.example.deliveryapp.review.dto.request.ReviewUpdateRequestDto;
import com.example.deliveryapp.review.dto.response.ReviewResponseDto;
import com.example.deliveryapp.review.dto.request.ReviewSaveRequestDto;
import com.example.deliveryapp.review.dto.response.ReviewSaveResponseDto;
import com.example.deliveryapp.review.dto.response.ReviewUpdateResponseDto;
import com.example.deliveryapp.review.service.ReviewService;
import com.example.deliveryapp.user.enums.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/api/v1/orders/{orderId}/reviews")
    public ResponseEntity<ReviewSaveResponseDto> save (
            @Auth AuthUser user, // JWT 토큰 검증
            @PathVariable Long orderId,
            @Valid @RequestBody ReviewSaveRequestDto dto
    ) {
        // ROLE_CUSTOMER만 리뷰 작성 가능
        if (user.getUserRole() != UserRole.ROLE_CUSTOMER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return ResponseEntity.ok(reviewService.save(user, orderId, dto));
    }

    // 리뷰 조회
    @GetMapping("/api/v1/stores/{storeId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> findAll(@Valid @PathVariable Long storeId) {
        return ResponseEntity.ok(reviewService.findAll(storeId));
    }

    // 리뷰 수정
    @PatchMapping("/api/v1/orders/{orderId}/review/{id}")
    public ResponseEntity<ReviewUpdateResponseDto> update (
            @Auth AuthUser user, // JWT 토큰 검증
            @PathVariable Long orderId,
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequestDto dto
    ) {
        return ResponseEntity.ok(reviewService.update(user, orderId, id, dto));
    }

    // 리뷰 삭제

    @DeleteMapping("/api/v1/orders/{orderId}/reviews/{id}")
    public void delete(@Auth AuthUser user, // JWT 토큰 검증
                       @Valid @PathVariable Long orderId,
                       @Valid @PathVariable Long id) {
        reviewService.deleteById(id, user.getId());
    }

}
