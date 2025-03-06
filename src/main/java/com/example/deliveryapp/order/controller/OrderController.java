package com.example.deliveryapp.order.controller;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.common.annotation.Auth;
import com.example.deliveryapp.common.annotation.OwnerUser;
import com.example.deliveryapp.order.aop.annotaion.OrderRequest;
import com.example.deliveryapp.order.dto.response.OrderInfoResponseDto;
import com.example.deliveryapp.order.dto.response.OrderResponseDto;
import com.example.deliveryapp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /*
     * 주문 등록하기 / 주문 상태 변경 - 손님 : 주문 요청
     */
    @OrderRequest
    @PostMapping()
    public ResponseEntity<OrderResponseDto> save(@Auth AuthUser authUser){
        OrderResponseDto responseDto = orderService.save(authUser);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /*
     * 주문 단건 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@Auth AuthUser authUser, @PathVariable Long orderId){
        OrderResponseDto responseDto = orderService.getOrder(authUser, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /*
     * 주문 다건 조회 (자신이 한 주문)
     *
     */
    @GetMapping()
    public ResponseEntity<Page<OrderInfoResponseDto>> getOrders(@Auth AuthUser authUser,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderInfoResponseDto> responseDto = orderService.getOrders(page, size, authUser);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /*
     * 주문 상태 변경 - 손님 : 주문 취소
     */
    @OrderRequest
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderInfoResponseDto> cancelOrder(@Auth AuthUser authUser, @PathVariable Long orderId){
        OrderInfoResponseDto responseDto = orderService.cancelOrder(authUser, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /*
     * 주문 상태 변경 - 사장님 : 주문 수락
     */
    @OrderRequest
    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<OrderInfoResponseDto> acceptOrder(@Auth AuthUser authUser, @PathVariable Long orderId){
        OrderInfoResponseDto responseDto = orderService.acceptOrder(authUser, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /*
     * 주문 상태 변경 - 사장님 : 주문 거절
     */
    @OrderRequest
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<OrderInfoResponseDto> rejectOrder(@Auth AuthUser authUser, @PathVariable Long orderId){
        OrderInfoResponseDto responseDto = orderService.rejectOrder(authUser, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /*
     * 주문 상태 변경 - 사장님 : 배달중
     */
    @OrderRequest
    @PatchMapping("/{orderId}/delivering")
    public ResponseEntity<OrderInfoResponseDto> deliveringOrder(@Auth AuthUser authUser, @PathVariable Long orderId){
        OrderInfoResponseDto responseDto = orderService.deliveringOrder(authUser, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /*
     * 주문 상태 변경 - 사장님 : 배달완료
     */
    @OrderRequest
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<OrderInfoResponseDto> completeOrder(@Auth AuthUser authUser, @PathVariable Long orderId){
        OrderInfoResponseDto responseDto = orderService.completeOrder(authUser, orderId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

}
