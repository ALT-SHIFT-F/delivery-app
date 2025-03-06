package com.example.deliveryapp.review.service;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.common.exception.custom_exception.CustomException;
import com.example.deliveryapp.common.exception.errorcode.ErrorCode;
import com.example.deliveryapp.order.entity.Order;
import com.example.deliveryapp.order.repository.OrderRepository;
import com.example.deliveryapp.review.dto.request.ReviewUpdateRequestDto;
import com.example.deliveryapp.review.dto.response.ReviewResponseDto;
import com.example.deliveryapp.review.dto.request.ReviewSaveRequestDto;
import com.example.deliveryapp.review.dto.response.ReviewSaveResponseDto;
import com.example.deliveryapp.review.dto.response.ReviewUpdateResponseDto;
import com.example.deliveryapp.review.entity.Review;
import com.example.deliveryapp.review.repository.ReviewRepository;
import com.example.deliveryapp.store.entity.Store;
import com.example.deliveryapp.store.repository.StoreRepository;
import com.example.deliveryapp.user.entity.User;
import com.example.deliveryapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.deliveryapp.order.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.deliveryapp.common.exception.errorcode.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository; //주문 정보 가져오기 위함
    private final StoreRepository storeRepository; //가게 정보 가져오기 위함

    @Transactional
    public ReviewSaveResponseDto save(AuthUser user, Long orderId, ReviewSaveRequestDto dto) {

        User Currentuser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        Store store = storeRepository.findById(dto.getStoreId())
                    .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

            // 주문 상태 체크: 완료된 주문만 리뷰 작성 가능
        if (!order.getState().isReviewable()) {
            throw new CustomException(ErrorCode.ORDER_NOT_REVIEWABLE);
        }
        Review review = Review.builder()
                .user(Currentuser)
                .store(store)
                .order(order)
                .content(dto.getContent())
                .score(dto.getScore())
                .build();

        Review savedReview = reviewRepository.save(review);

        return new ReviewSaveResponseDto(
                savedReview.getId(),
                user.getId(),
                store.getId(),
                order.getId(),
                savedReview.getContent(),
                savedReview.getScore(),
                savedReview.getCreatedAt()
        );

    }

    // 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> findAll(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
        // 리뷰 최신순으로 정렬
        List<Review> reviews = reviewRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
        List<ReviewResponseDto> dtos = new ArrayList<>();

        for (Review review : reviews) {
            ReviewResponseDto dto = new ReviewResponseDto(
                    review.getId(),
                   review.getUser().getId(),
                    review.getScore(),
                    review.getContent(),
                    review.getCreatedAt(),
                    review.getUpdatedAt());
            dtos.add(dto);
        }

        return dtos;
    }

    // 리뷰 수정
    @Transactional
    public ReviewUpdateResponseDto update(AuthUser user, Long orderId, Long id, ReviewUpdateRequestDto dto) {

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

         // 리뷰 존재 여부 확인
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        //작성자 검증
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_UPDATE);
        }

        review.update(dto.getContent(), dto.getScore());
        return  new ReviewUpdateResponseDto(
                review.getId(),
                review.getStore().getId(),
                review.getScore(),
                review.getContent(),
                review.getUpdatedAt()
        );

    }

    // 리뷰 삭제
    @Transactional
    public void deleteById(Long id, Long userId) {
        // 리뷰 존재 여부 확인
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        //작성자 검증
        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_DELETE);
        }
        // 리뷰 삭제
        reviewRepository.deleteById(id);
    }
    }

