package com.example.deliveryapp.common.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 Bad Request (잘못된 요청)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    EMPTY_REVIEW_CONTENT(HttpStatus.BAD_REQUEST, "리뷰를 입력하세요."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),

    // 403 Forbidden (권한 없음)
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    UNAUTHORIZED_REVIEW_UPDATE(HttpStatus.FORBIDDEN, "리뷰 수정 권한이 없습니다."),
    UNAUTHORIZED_REVIEW_DELETE(HttpStatus.FORBIDDEN, "리뷰 삭제 권한이 없습니다."),
    ORDER_NOT_REVIEWABLE(HttpStatus.FORBIDDEN, "배달 완료된 주문만 리뷰를 작성할 수 있습니다."),
    INVALID_MEMBER_ACCESS(HttpStatus.FORBIDDEN, "일반 회원만 가능한 기능입니다."),
    INVALID_OWNER_ACCESS(HttpStatus.FORBIDDEN, "사장 회원만 가능한 기능입니다."),

    // 404 Not Found (리소스 없음)
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시물이 존재하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문이 존재하지 않습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메뉴가 존재하지 않습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장바구니가 존재하지 않습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가게가 존재하지 않습니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주소가 존재하지 않습니다."),

    // 장바구니 관련 요청
    INVALID_STORE_MENU(HttpStatus.BAD_REQUEST, "동일한 가게의 메뉴만 담을 수 있습니다."),
    EXCEEDS_STORE_STOCK(HttpStatus.BAD_REQUEST, "가게의 재고보다 더 많이 주문할 수 없습니다."),
    INVALID_CART_ACCESS(HttpStatus.FORBIDDEN, "자신의 장바구니만 수정할 수 있습니다."),

    // 주문 관련 요청
    INVALID_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "최소주문금액보다 적은 금액은 주문할 수 없습니다."),
    INVALID_OPERATING_HOURS(HttpStatus.BAD_REQUEST, "운영 시간이 아니므로 주문할 수 없습니다."),
    INVALID_ORDER_ACCESS(HttpStatus.FORBIDDEN, "자신의 가게의 주문만 상태를 변경할 수 있습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고보다 더 많은 수를 주문할 수 없습니다."),
    UNAUTHORIZED_ORDER_ACCESS(HttpStatus.FORBIDDEN, "자신의 주문만 접근할 수 있습니다."),
    INVALID_CANCEL_STATE(HttpStatus.BAD_REQUEST, "주문 요청 상태에서만 취소가 가능합니다."),
    INVALID_ACCEPT_STATE(HttpStatus.BAD_REQUEST, "주문 요청 상태에서만 수락이 가능합니다."),
    INVALID_REJECT_STATE(HttpStatus.BAD_REQUEST, "주문 요청 상태 혹은 수락 상태에서만 거절이 가능합니다."),
    INVALID_DELIVERY_START_STATE(HttpStatus.BAD_REQUEST, "주문 수락 상태에서만 배달시작이 가능합니다."),
    INVALID_DELIVERY_COMPLETE_STATE(HttpStatus.BAD_REQUEST, "배달 중 상태에서만 배달완료가 가능합니다."),

    // 인증/인가 관련 요청
    EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST,"이미 존재하는 이메일입니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    LOGGED_OUT_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "로그아웃된 사용자의 Refresh Token입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST,"유효하지 않은 Refresh Token입니다."),

    // 주소 관련 요청
    USER_ADDRESS_MISMATCH(HttpStatus.BAD_REQUEST, "사용자의 올바른 주소가 아닙니다."),
    MAX_ADDRESS_COUNT(HttpStatus.BAD_REQUEST,"주소는 기본배송지를 포함 최대 10개까지 생성할 수 있습니다."),
    FORBIDDEN_DEFAULT_ADDRESS_DELETION(HttpStatus.BAD_REQUEST, "기본 배송지는 삭제 불가능합니다."),
    FORBIDDEN_LAST_ADDRESS_DELETION(HttpStatus.BAD_REQUEST,"배송지는 최소 1개 이상이어야 합니다.");



    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;

    }

}
