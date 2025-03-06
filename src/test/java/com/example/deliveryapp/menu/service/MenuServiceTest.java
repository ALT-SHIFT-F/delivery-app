package com.example.deliveryapp.menu.service;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.menu.dto.request.MenuRequestDto;
import com.example.deliveryapp.menu.dto.response.MenuSimpleResponseDto;
import com.example.deliveryapp.menu.entity.Menu;
import com.example.deliveryapp.menu.enums.MenuCategory;
import com.example.deliveryapp.menu.enums.MenuStatus;
import com.example.deliveryapp.menu.exception.DuplicateMenuException;
import com.example.deliveryapp.menu.repository.MenuRepository;
import com.example.deliveryapp.store.entity.Store;
import com.example.deliveryapp.store.repository.StoreRepository;
import com.example.deliveryapp.user.entity.User;
import com.example.deliveryapp.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void 사장님_계정으로_메뉴를_저장할_수_있다() {
        // given
        User owner = createMockOwner();
        Store store = createMockStore(owner);
        Long storeId = store.getId();
        AuthUser authUser = new AuthUser(owner.getId(), owner.getEmail(), owner.getUserRole());
        MenuRequestDto requestDto = new MenuRequestDto("후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, MenuStatus.AVAILABLE, 10, "바삭바삭한_후라이드_치킨");

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.existsByStoreIdAndMenuNameAndMenuStatusNot(storeId, requestDto.getMenuName(), MenuStatus.DELETED))
                .willReturn(false);
        given(menuRepository.save(any(Menu.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        MenuSimpleResponseDto response = menuService.saveMenu(requestDto, storeId, authUser);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMenuName()).isEqualTo("후라이드_치킨");
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    void 고객_계정으로_메뉴를_저장하면_IllegalArgumentException을_던진다() {
        // given
        User owner = createMockOwner();
        Store store = createMockStore(owner);
        User customer = createMockCustomer();
        AuthUser authUser = new AuthUser(customer.getId(), customer.getEmail(), customer.getUserRole());
        Long storeId = store.getId();
        MenuRequestDto requestDto = new MenuRequestDto("후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, MenuStatus.AVAILABLE, 10, "바삭바삭한_후라이드_치킨");

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> menuService.saveMenu(requestDto, storeId, authUser));
    }

    @Test
    void 다른_사장님이_메뉴를_저장하면_IllegalArgumentException을_던진다() {
        // given
        User owner = createMockOwner();
        User anotherOwner = createMockAnotherOwner();
        Store store = createMockStore(owner);
        Long storeId = store.getId();
        AuthUser authUser = new AuthUser(anotherOwner.getId(), anotherOwner.getEmail(), anotherOwner.getUserRole());

        MenuRequestDto requestDto = new MenuRequestDto("후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, MenuStatus.AVAILABLE, 10, "설명");

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> menuService.saveMenu(requestDto, storeId, authUser));
    }

    @Test
    void 중복된_메뉴_저장_시_DuplicateMenuException을_던진다() {
        // given
        User owner = createMockOwner();
        Store store = createMockStore(owner);
        ReflectionTestUtils.setField(store, "id", 1L);
        Long storeId = store.getId();
        AuthUser authUser = new AuthUser(owner.getId(), owner.getEmail(), owner.getUserRole());
        MenuRequestDto requestDto = new MenuRequestDto("후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, MenuStatus.AVAILABLE, 10, "바삭바삭한_후라이드_치킨");

        given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
        given(menuRepository.existsByStoreIdAndMenuNameAndMenuStatusNot(anyLong(), eq("후라이드_치킨"), eq(MenuStatus.DELETED)))
                .willReturn(true);

        // when & then
        assertThrows(DuplicateMenuException.class, () -> menuService.saveMenu(requestDto, storeId, authUser));
    }

    @Test
    void 특정_스토어의_사용_가능한_메뉴를_조회할_수_있다() {
        // given
        Long storeId = 1L;
        List<Menu> menus = List.of(
                new Menu("후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, MenuStatus.AVAILABLE, 10, "바삭바삭한_후라이드_치킨"),
                new Menu("양념_치킨", BigDecimal.valueOf(17000), MenuCategory.MAIN, MenuStatus.AVAILABLE, 5, "매콤달콤한_양념치킨")
        );

        given(menuRepository.findByStoreIdOrderByCategoryActiveOnly(storeId)).willReturn(menus);

        // when
        List<MenuSimpleResponseDto> response = menuService.findAvailableMenusByStoreId(storeId);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getMenuName()).isEqualTo("후라이드_치킨");
        assertThat(response.get(1).getMenuName()).isEqualTo("양념_치킨");
    }

    @Test
    void 존재하지_않는_스토어의_메뉴를_조회하면_빈_리스트를_반환한다() {
        // given
        Long storeId = 99L;
        given(menuRepository.findByStoreIdOrderByCategoryActiveOnly(storeId)).willReturn(List.of());

        // when
        List<MenuSimpleResponseDto> response = menuService.findAvailableMenusByStoreId(storeId);

        // then
        assertThat(response).isEmpty();
    }

    @Test
    void 메뉴를_삭제할_수_있다() {
        // given
        Long menuId = 1L;
        Menu menu = new Menu("후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, MenuStatus.AVAILABLE, 10, "바삭바삭한_후라이드_치킨");

        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));
        given(menuRepository.save(any(Menu.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        menuService.delete(menuId);

        // then
        assertThat(menu.getMenuStatus()).isEqualTo(MenuStatus.DELETED);
    }

    @Test
    void 존재하지_않는_메뉴를_삭제_시_IllegalArgumentException을_던진다() {
        // given
        Long menuId = 99L;
        given(menuRepository.findById(menuId)).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> menuService.delete(menuId));
    }

    @Test
    void 이미_삭제된_메뉴를_삭제_시_IllegalArgumentException을_던진다() {
        // given
        Long menuId = 1L;
        Menu menu = new Menu("후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, MenuStatus.DELETED, 10, "바삭바삭한_후라이드_치킨");

        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> menuService.delete(menuId));
    }

    private User createMockOwner() {
        User owner = new User("사장님", UserRole.ROLE_OWNER, "owner@example.com", "password", "010-1111-1111");
        ReflectionTestUtils.setField(owner, "id", 1L);
        return owner;}

    private User createMockAnotherOwner() {
        User owner = new User("다른가게_사장님", UserRole.ROLE_OWNER, "another_owner@example.com", "password", "098-76-54321", "010-3333-3333");
        ReflectionTestUtils.setField(owner, "id", 200L);
        return owner;
    }

    private User createMockCustomer() {
        return new User("일반_유저", UserRole.ROLE_CUSTOMER, "customer@example.com", "password", "서울시 중구", "010-2222-2222");
    }

    private Store createMockStore(User owner) {
        return new Store("땡땡_치킨", "치킨",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                10000.0, owner);
    }
}