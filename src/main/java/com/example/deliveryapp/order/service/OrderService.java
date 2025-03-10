package com.example.deliveryapp.order.service;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.cart.entity.Cart;
import com.example.deliveryapp.cart.repository.CartRepository;
import com.example.deliveryapp.common.exception.custom_exception.InvalidRequestException;
import com.example.deliveryapp.common.exception.custom_exception.ServerException;
import com.example.deliveryapp.common.exception.errorcode.ErrorCode;
import com.example.deliveryapp.menu.entity.Menu;
import com.example.deliveryapp.menu.repository.MenuRepository;
import com.example.deliveryapp.menu.service.MenuService;
import com.example.deliveryapp.order.dto.response.OrderDetailResponseDto;
import com.example.deliveryapp.order.dto.response.OrderInfoResponseDto;
import com.example.deliveryapp.order.dto.response.OrderResponseDto;
import com.example.deliveryapp.order.entity.Order;
import com.example.deliveryapp.order.entity.OrderDetail;
import com.example.deliveryapp.order.enums.OrderStatus;
import com.example.deliveryapp.order.repository.OrderDetailRepository;
import com.example.deliveryapp.order.repository.OrderRepository;
import com.example.deliveryapp.store.entity.Store;
import com.example.deliveryapp.store.repository.StoreRepository;
import com.example.deliveryapp.user.entity.User;
import com.example.deliveryapp.user.enums.UserRole;
import com.example.deliveryapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MenuService menuService;

    @Transactional
    public OrderResponseDto save(AuthUser authUser) {
        // 일반 회원인지 검증
        isValidCustomer(authUser);

        // 사용자 아이디로 장바구니에 있는 목록을 조회해서 첫번째 메뉴를 통해서 가게 아이디를 뽑아옴
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));
        List<Cart> carts = cartRepository.findByUserId(authUser.getId());

        // 장바구니가 비어있는지 검증
        isEmptyCart(carts);
        Long storeId = carts.get(0).getMenu().getStore().getId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new InvalidRequestException(ErrorCode.STORE_NOT_FOUND));

        // 가게 운영시간인지 검증
        isStoreOpenNow(store);

        // 뽑아온 가게 아이디를 이용해서 주문 객체 생성
        Order order = Order.builder()
                .store(store)
                .user(user)
                .orderNumber(generateMerchantUid())
                .state(OrderStatus.of(0))
                .build();

        Order savedOrder = orderRepository.save(order);

        // 메뉴 재고 체크 한번 더해서 재고보다 적은데 주문하는 것은 아닌지 검증
        validateStockAvailability(carts);

        // 가지고 온 목록을 이용해서 주문 상세 엔티티 생성하기
        for(Cart cart : carts){
            OrderDetail orderDetail = new OrderDetail(cart.getMenu(), savedOrder, cart.getQuantity(), cart.getMenu().getPrice());
            orderDetailRepository.save(orderDetail);

            // 비관적 락을 사용하여, 메뉴 조회
            Menu menu = menuService.findMenuWithLock(cart.getMenu().getId());

            //판매량 증가 및 재고 수량 업데이트
            menu.updateSalesAndStock(cart.getQuantity(),false);
        }

        // 주문 정보 조회하기
        List<Order> orders = orderRepository.findByUserId(authUser.getId());
        Order readOrder = orders.get(orders.size() - 1); // 가장 최신 정보 가져오기
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(readOrder.getId());

        // 총 주문 금액 구하기
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderDetail orderDetail : orderDetails){
            BigDecimal itemTotal = orderDetail.getPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }

        // 최소 주문금액을 충족했는지 검증
        validateOrderPrice(totalPrice, store);

        // 주문 엔티티에 총 주문 금액 입력
        order.updateTotalPrice(totalPrice);
        // 장바구니 비우기
        cartRepository.deleteByUserId(authUser.getId());

        List<OrderDetailResponseDto> orderDetailResponseDtos = OrderDetailResponseDto.toResponse(orderDetails);
        OrderResponseDto orderResponseDto = OrderResponseDto.builder()
                .orderId(readOrder.getId())
                .orderNumber(readOrder.getOrderNumber())
                .memberName(readOrder.getUser().getUserName())
                .storeName(readOrder.getStore().getBusinessName())
                .state(readOrder.getState().getDescription())
                .totalPrice(totalPrice)
                .orderDetailResponseDtos(orderDetailResponseDtos)
                .build();
        return orderResponseDto;
    }

    private static void isEmptyCart(List<Cart> carts) {
        if (carts.isEmpty()) {
            throw new InvalidRequestException(ErrorCode.CART_NOT_FOUND);
        }
    }

    public OrderResponseDto getOrder(AuthUser authUser, Long orderId) {
        // 일반 회원인지 검증
        isValidCustomer(authUser);
        Order readOrder = orderRepository.findByOrderId(orderId);

        // 자기 자신의 주문을 접근했는지 검증
        validateOwnOrderAccess(authUser, readOrder);

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(readOrder.getId());
        List<OrderDetailResponseDto> orderDetailResponseDtos = OrderDetailResponseDto.toResponse(orderDetails);
        OrderResponseDto orderResponseDto = OrderResponseDto.builder()
                .orderId(readOrder.getId())
                .orderNumber(readOrder.getOrderNumber())
                .memberName(readOrder.getUser().getUserName())
                .storeName(readOrder.getStore().getBusinessName())
                .state(readOrder.getState().getDescription())
                .totalPrice(readOrder.getTotalPrice())
                .orderDetailResponseDtos(orderDetailResponseDtos)
                .build();
        return orderResponseDto;
    }

    public Page<OrderInfoResponseDto> getOrders(int page, int size, AuthUser authUser) {
        // 일반 회원인지 검증
        isValidCustomer(authUser);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Order> orderPage = orderRepository.findByUserIdPaged(pageable, authUser.getId());
        Page<OrderInfoResponseDto> orderResponseDtoPage = OrderInfoResponseDto.toResponsePage(orderPage, pageable);
        return orderResponseDtoPage;
    }

    @Transactional
    public OrderInfoResponseDto cancelOrder(AuthUser authUser, Long orderId) {
        // 일반 회원인지 검증
        isValidCustomer(authUser);

        Order order = orderRepository.findByOrderId(orderId);

        // 주문 취소가 가능한 상태인지 검증
        validateOrderCancelable(order);

        // 주문 상세 내역 조회 및 재고/판매량 복구
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail orderDetail : orderDetails) {
            Menu menu = orderDetail.getMenu();
            menu.updateSalesAndStock(orderDetail.getQuantity(),true);
        }

        order.update(OrderStatus.of(3));
        OrderInfoResponseDto orderInfoResponseDto = OrderInfoResponseDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberName(order.getUser().getUserName())
                .storeName(order.getStore().getBusinessName())
                .state(order.getState().getDescription())
                .totalPrice(order.getTotalPrice())
                .build();
        return orderInfoResponseDto;
    }

    @Transactional
    public OrderInfoResponseDto acceptOrder(AuthUser authUser, Long orderId) {
        // 사장 회원인지 검증
        isValidOwner(authUser);
        Order order = orderRepository.findByOrderId(orderId);
        // 자신의 가게의 주문정보인지 검증
        validateOwnerCanChangeOrderStatus(authUser, order);
        // 상태변경이 가능한지 검증
        validateOrderAcceptable(order);

        // 주문 상태 업데이트
        order.update(OrderStatus.of(1));
        OrderInfoResponseDto orderInfoResponseDto = OrderInfoResponseDto.builder()
                                            .orderId(order.getId())
                                            .orderNumber(order.getOrderNumber())
                                            .memberName(order.getUser().getUserName())
                                            .storeName(order.getStore().getBusinessName())
                                            .state(order.getState().getDescription())
                                            .totalPrice(order.getTotalPrice())
                                            .build();
        return orderInfoResponseDto;
    }

    @Transactional
    public OrderInfoResponseDto rejectOrder(AuthUser authUser, Long orderId) {
        // 사장 회원인지 검증
        isValidOwner(authUser);
        Order order = orderRepository.findByOrderId(orderId);
        // 자신의 가게의 주문정보인지 검증
        validateOwnerCanChangeOrderStatus(authUser, order);
        // 상태변경이 가능한지 검증
        validateOrderRejectable(order);

        // 주문 상세 내역 조회 및 재고/판매량 복구
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail orderDetail : orderDetails) {
            Menu menu = orderDetail.getMenu();
            menu.updateSalesAndStock(orderDetail.getQuantity(),true);
        }

        // 주문 상태 업데이트
        order.update(OrderStatus.of(2));
        OrderInfoResponseDto orderInfoResponseDto = OrderInfoResponseDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberName(order.getUser().getUserName())
                .storeName(order.getStore().getBusinessName())
                .state(order.getState().getDescription())
                .totalPrice(order.getTotalPrice())
                .build();
        return orderInfoResponseDto;
    }

    @Transactional
    public OrderInfoResponseDto deliveringOrder(AuthUser authUser, Long orderId) {
        // 사장 회원인지 검증
        isValidOwner(authUser);
        Order order = orderRepository.findByOrderId(orderId);

        // 메뉴 재고 체크 한번 더해서 재고보다 적은데 주문하는 것은 아닌지 검증
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail orderDetail : orderDetails){
            if (orderDetail.getQuantity() > orderDetail.getMenu().getStockQuantity()){
                throw new ServerException(ErrorCode.INSUFFICIENT_STOCK);
            } else {
                Menu orderdMenu = orderDetail.getMenu();
                orderdMenu.updateStockQuantity((int) (orderDetail.getMenu().getStockQuantity() - orderDetail.getQuantity()));
            }
        }

        // 자신의 가게의 주문정보인지 검증
        validateOwnerCanChangeOrderStatus(authUser, order);
        // 상태 변경이 가능한지 검증
        validateOrderDeliverable(order);


        order.update(OrderStatus.of(4));
        OrderInfoResponseDto orderInfoResponseDto = OrderInfoResponseDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberName(order.getUser().getUserName())
                .storeName(order.getStore().getBusinessName())
                .state(order.getState().getDescription())
                .totalPrice(order.getTotalPrice())
                .build();
        return orderInfoResponseDto;
    }

    @Transactional
    public OrderInfoResponseDto completeOrder(AuthUser authUser, Long orderId) {
        // 사장 회원인지 검증
        isValidOwner(authUser);

        Order order = orderRepository.findByOrderId(orderId);
        // 자신의 가게의 주문정보인지 검증
        validateOwnerCanChangeOrderStatus(authUser, order);
        // 상태 변경이 가능한지 검증
        validateOrderComplete(order);
        order.update(OrderStatus.of(5));
        OrderInfoResponseDto orderInfoResponseDto = OrderInfoResponseDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberName(order.getUser().getUserName())
                .storeName(order.getStore().getBusinessName())
                .state(order.getState().getDescription())
                .totalPrice(order.getTotalPrice())
                .build();
        return orderInfoResponseDto;
    }

    /*
     * 일반 회원인지 검증하는 메서드
     */
    private static void isValidCustomer(AuthUser authUser) {
        if (!UserRole.ROLE_CUSTOMER.equals(authUser.getUserRole())){
            throw new ServerException(ErrorCode.INVALID_MEMBER_ACCESS);
        }
    }

    /*
     * 사장 회원인지 검증하는 메서드
     */
    private static void isValidOwner(AuthUser authUser) {
        if (!UserRole.ROLE_OWNER.equals(authUser.getUserRole())){
            throw new ServerException(ErrorCode.INVALID_OWNER_ACCESS);
        }
    }


    /*
     * 메뉴 재고 체크를 한번 더 해서 재고보다 적은데 주문하는 것은 아닌지 검증하는 메서드
     */
    private static void validateStockAvailability(List<Cart> carts) {
        for(Cart cart : carts){
            if (cart.getQuantity() > cart.getMenu().getStockQuantity()) {
                throw new ServerException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }
    }

    /*
     * 최소 주문금액을 충족했는지 검증
     */
    private static void validateOrderPrice(BigDecimal totalPrice, Store store) {
        // 총 주문 금액이 최소 주문 금액보다 큰지 검증
        if (totalPrice.compareTo(BigDecimal.valueOf(store.getMinOrderPrice())) < 0)  {
            throw new ServerException(ErrorCode.INVALID_ORDER_AMOUNT);
        }
    }

    /*
     * 가게 운영시간인지 검증
     */
    private static void isStoreOpenNow(Store store) {
        if (!(LocalTime.now().isAfter(store.getOpeningTime()) && LocalTime.now().isBefore(store.getClosingTime()))) {
            throw new ServerException(ErrorCode.INVALID_OPERATING_HOURS);
        }
    }

    /*
     * 자기 자신의 주문을 접근하는지 검증하는 메서드
     */
    private static void validateOwnOrderAccess (AuthUser authUser, Order order) {
        if (!authUser.getId().equals(order.getUser().getId())){
            throw new ServerException(ErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }
    }

    /*
     * 주문번호 생성 메서드
     */
    private String generateMerchantUid() {
        // 현재 날짜와 시간을 포함한 고유한 문자열 생성
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDay = today.format(formatter).replace("-", "");

        // 무작위 문자열과 현재 날짜/시간을 조합하여 주문번호 생성
        return formattedDay +'-'+ uniqueString;
    }

    /*
     * 자기 자신의 가게인지 검증하는 메서드
     */
    private static void validateOwnerCanChangeOrderStatus(AuthUser authUser, Order order) {
        if (!authUser.getId().equals(order.getStore().getOwner().getId())){
            throw new ServerException(ErrorCode.INVALID_ORDER_ACCESS);
        }
    }

    /*
     * 주문 상태를 변경해도 되는지 검증하는 메서드
     */
    private static void validateOrderCancelable(Order order) {
        if (!OrderStatus.ORDER_REQUESTED.equals(order.getState())){
            throw new ServerException(ErrorCode.INVALID_CANCEL_STATE);
        }
    }

    private static void validateOrderAcceptable(Order order) {
        if (!OrderStatus.ORDER_REQUESTED.equals(order.getState())){
            throw new ServerException(ErrorCode.INVALID_ACCEPT_STATE);
        }
    }

    private static void validateOrderRejectable(Order order) {
        if (!(OrderStatus.ORDER_REQUESTED.equals(order.getState()) || OrderStatus.ORDER_ACCEPTED.equals(order.getState()))) {
            throw new ServerException(ErrorCode.INVALID_REJECT_STATE);
        }
    }

    private static void validateOrderDeliverable(Order order) {
        if (!OrderStatus.ORDER_ACCEPTED.equals(order.getState())){
            throw new ServerException(ErrorCode.INVALID_DELIVERY_START_STATE);
        }
    }

    private static void validateOrderComplete(Order order) {
        if (!OrderStatus.DELIVERING.equals(order.getState())){
            throw new ServerException(ErrorCode.INVALID_DELIVERY_COMPLETE_STATE);
        }
    }
}
