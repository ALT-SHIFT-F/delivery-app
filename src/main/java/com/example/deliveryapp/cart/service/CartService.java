package com.example.deliveryapp.cart.service;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.cart.dto.request.CartSaveRequestDto;
import com.example.deliveryapp.cart.dto.request.CartUpdateRequestDto;
import com.example.deliveryapp.cart.dto.response.CartResponseDto;
import com.example.deliveryapp.cart.entity.Cart;
import com.example.deliveryapp.cart.repository.CartRepository;
import com.example.deliveryapp.common.exception.custom_exception.InvalidRequestException;
import com.example.deliveryapp.common.exception.custom_exception.ServerException;
import com.example.deliveryapp.common.exception.errorcode.ErrorCode;
import com.example.deliveryapp.menu.entity.Menu;
import com.example.deliveryapp.menu.repository.MenuRepository;
import com.example.deliveryapp.user.entity.User;
import com.example.deliveryapp.user.enums.UserRole;
import com.example.deliveryapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.deliveryapp.cart.dto.response.CartResponseDto.toResponsePage;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public CartResponseDto save(AuthUser authUser, CartSaveRequestDto requestDto) {
        // 일반 회원이 요청했는지 검증
        isValidCustomer(authUser);
        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));
        Menu menu = menuRepository.findById(requestDto.getMenuId()).orElseThrow(() -> new InvalidRequestException(ErrorCode.MENU_NOT_FOUND));

        // 장바구니에 담은 메뉴가 이미 담은 메뉴의 가게와 동일한지 검증
        isSameStoreMenu(requestDto, user);

        // 가게의 재고보다 더 많이 장바구니에 넣었는지 검증
        isStockEnoughForOrder(requestDto.getQuantity(), menu);

        Cart cart = new Cart(menu, user, requestDto.getQuantity());
        Cart savedCart = cartRepository.save(cart);
        Cart readCart = cartRepository.findByIdWithMenuAndMember(savedCart.getId());
        return CartResponseDto.builder()
                .cartId(readCart.getId())
                .menuId(readCart.getMenu().getId())
                .menuName(readCart.getMenu().getMenuName())
                .memberId(readCart.getUser().getId())
                .memberName(readCart.getUser().getUserName())
                .quantity(readCart.getQuantity())
                .build();
    }

    public Page<CartResponseDto> getCarts(int page, int size, AuthUser authUser) {
        // 일반 회원이 요청했는지 검증
        isValidCustomer(authUser);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Cart> cartPage = cartRepository.findByMemberId(pageable, authUser.getId());
        Page<CartResponseDto> responsePage = toResponsePage(cartPage, pageable);
        return responsePage;
    }

    @Transactional
    public CartResponseDto update(AuthUser authUser, CartUpdateRequestDto updateRequestDto, Long cartId) {
        // 일반 회원이 요청했는지 검증
        isValidCustomer(authUser);
        Menu menu = menuRepository.findById(updateRequestDto.getMenuId()).orElseThrow(() -> new InvalidRequestException(ErrorCode.MENU_NOT_FOUND));
        Cart cart = cartRepository.findByIdWithMember(cartId).orElseThrow(() -> new InvalidRequestException(ErrorCode.CART_NOT_FOUND));
        // 자기 자신의 장바구니를 수정하는지 검증
        isCartOwnedByUser(authUser, cart);
        // 가게의 재고보다 더 많이 장바구니에 넣었는지 검증
        isStockEnoughForOrder(updateRequestDto.getQuantity(), menu);
        cart.update(menu, updateRequestDto.getQuantity());
        Cart readCart = cartRepository.findByIdWithMenuAndMember(cart.getId());
        return CartResponseDto.builder()
                .cartId(readCart.getId())
                .menuId(readCart.getMenu().getId())
                .menuName(readCart.getMenu().getMenuName())
                .memberId(readCart.getUser().getId())
                .memberName(readCart.getUser().getUserName())
                .quantity(readCart.getQuantity())
                .build();
    }

    public void delete(AuthUser authUser, Long cartId) {
        // 일반 회원이 요청했는지 검증
        isValidCustomer(authUser);
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new InvalidRequestException(ErrorCode.CART_NOT_FOUND));
        // 자기 자신의 장바구니를 삭제하는지 검증
        isCartOwnedByUser(authUser, cart);
        cartRepository.delete(cart);
    }

    /*
     * 장바구니에 담은 메뉴가 이미 담은 메뉴의 가게와 동일한지 검증하는 메서드
     */
    private void isSameStoreMenu(CartSaveRequestDto requestDto, User user) {
        List<Cart> savedCarts = cartRepository.findByMemberIdWithStore(user.getId());
        if (!savedCarts.isEmpty()){
            Long storeId = savedCarts.get(0).getMenu().getStore().getId();
            Menu savedMenu = menuRepository.findByIdWithStore(requestDto.getMenuId());
            if (!storeId.equals(savedMenu.getStore().getId())){
                throw new ServerException(ErrorCode.INVALID_STORE_MENU);
            }
        }
    }

    /*
     * 가게의 재고보다 더 많이 장바구니에 넣었는지 검증
     */
    private static void isStockEnoughForOrder(Long orderQuantity, Menu menu) {
        if (menu.getStockQuantity() < orderQuantity){
            throw new ServerException(ErrorCode.EXCEEDS_STORE_STOCK);
        }
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
     * 자기 자신의 장바구니를 수정하는지 검증하는 메서드
     */
    private static void isCartOwnedByUser(AuthUser authUser, Cart cart) {
        if (!authUser.getId().equals(cart.getUser().getId())){
            throw new ServerException(ErrorCode.INVALID_CART_ACCESS);
        }
    }
}
