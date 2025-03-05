package com.example.deliveryapp.cart.service;

import com.example.deliveryapp.cart.dto.request.CartSaveRequestDto;
import com.example.deliveryapp.cart.dto.request.CartUpdateRequestDto;
import com.example.deliveryapp.cart.dto.response.CartResponseDto;
import com.example.deliveryapp.cart.entity.Cart;
import com.example.deliveryapp.cart.repository.CartRepository;
import com.example.deliveryapp.menu.entity.Menu;
import com.example.deliveryapp.menu.repository.MenuRepository;
import com.example.deliveryapp.user.entity.User;
import com.example.deliveryapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.deliveryapp.cart.dto.response.CartResponseDto.toResponsePage;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public CartResponseDto save(CartSaveRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getMemberId()).orElseThrow(() -> new RuntimeException("데이터가 없습니다"));
        Menu menu = menuRepository.findById(requestDto.getMenuId()).orElseThrow(() -> new RuntimeException("데이터가 없습니다"));
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

    public Page<CartResponseDto> getCarts(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Cart> cartPage = cartRepository.findByMemberId(pageable, userId);
        Page<CartResponseDto> responsePage = toResponsePage(cartPage, pageable);
        return responsePage;
    }

    @Transactional
    public CartResponseDto update(CartUpdateRequestDto updateRequestDto, Long cartId) {
        Menu menu = menuRepository.findById(updateRequestDto.getMenuId()).orElseThrow(() -> new RuntimeException("데이터가 없습니다"));
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new RuntimeException("데이터가 없습니다"));
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

    public void delete(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new RuntimeException("데이터가 없습니다"));
        cartRepository.delete(cart);
    }
}
