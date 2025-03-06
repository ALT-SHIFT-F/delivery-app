package com.example.deliveryapp.menu.service;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.menu.dto.request.MenuRequestDto;
import com.example.deliveryapp.menu.dto.request.MenuSimpleUpdateRequestDto;
import com.example.deliveryapp.menu.dto.response.MenuResponseDto;
import com.example.deliveryapp.menu.dto.response.MenuSimpleResponseDto;
import com.example.deliveryapp.menu.entity.Menu;
import com.example.deliveryapp.menu.enums.MenuStatus;
import com.example.deliveryapp.menu.exception.DuplicateMenuException;
import com.example.deliveryapp.menu.repository.MenuRepository;
import com.example.deliveryapp.store.entity.Store;
import com.example.deliveryapp.store.repository.StoreRepository;
import com.example.deliveryapp.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public MenuSimpleResponseDto saveMenu(MenuRequestDto dto, Long storeId, AuthUser user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found")
                );

        // 고객 계정이면 예외 발생
        if (user.getUserRole() == UserRole.ROLE_CUSTOMER) {
            throw new IllegalArgumentException("고객 계정은 메뉴를 추가할 수 없습니다.");
        }

        // 로그인한 사용자의 ID와 가게 주인 ID가 다르면 예외 발생
        if (!store.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 가게의 메뉴를 추가할 권한이 없습니다.");
        }

        // 중복된 메뉴 이름 검증.
        if (menuRepository.existsByStoreIdAndMenuNameAndMenuStatusNot(storeId,dto.getMenuName(),MenuStatus.DELETED)) {
            throw new DuplicateMenuException(HttpStatus.BAD_REQUEST,"DUPLICATED_MENU","이미 존재하는 메뉴 이름입니다.");
        }

        Menu menu = new Menu(
                dto.getMenuName(),
                dto.getPrice(),
                dto.getMenuCategory(),
                dto.getMenuStatus(),
                dto.getStockQuantity(),
                dto.getDescription()
        );

        Menu savedMenu = menuRepository.save(menu);
        return MenuSimpleResponseDto.toDto(savedMenu);
    }

    // 가게 메뉴 페이지 to 클라이언트
    @Transactional(readOnly = true)
    public List<MenuSimpleResponseDto> findAvailableMenusByStoreId(Long storeId) {
        List<Menu> menus = menuRepository.findByStoreIdOrderByCategoryActiveOnly(storeId);
        return menus.stream().map(MenuSimpleResponseDto::toDto)
                .collect(Collectors.toList());
    }

    // 가게 메뉴 페이지 to 사장님
    @Transactional(readOnly = true)
    public List<MenuResponseDto> findAllMenusByStoreId(Long storeId) {
        List<Menu> menus = menuRepository.findByStoreIdOrderByCategory(storeId);
        return menus.stream().map(MenuResponseDto::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuSimpleResponseDto updateMenuSimple(MenuSimpleUpdateRequestDto dto, Long menuId) {
        Menu menu = menuRepository.findById(menuId).orElseThrow(
                () -> new IllegalArgumentException("Menu not found")
        );

        menu.simpleUpdate(
                dto.getMenuStatus(),
                dto.getStockQuantity()
        );

        return MenuSimpleResponseDto.toDto(menuRepository.save(menu));
    }

    @Transactional
    public MenuResponseDto updateMenuDetail(MenuRequestDto dto, Long menuId) {
        Menu menu = menuRepository.findById(menuId).orElseThrow(
                () -> new IllegalArgumentException("Menu not found")
        );

        menu.update(
                dto.getMenuName(),
                dto.getPrice(),
                dto.getMenuCategory(),
                dto.getMenuStatus(),
                dto.getStockQuantity(),
                dto.getDescription()
        );
        return MenuResponseDto.toDto(menuRepository.save(menu));
    }

    @Transactional
    public void delete(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found"));
        if (menu.getMenuStatus() == MenuStatus.DELETED) {
            throw new IllegalArgumentException("이미 삭제된 메뉴입니다.");
        }

        menu.delete(); // 'DELETED'로 변경
        menuRepository.save(menu); // 변경 내용 저장
    }

    @Transactional
    public void deleteAll(Long storeId) {
        // StoreId로 메뉴 상태가 'AVAILABLE' or 'INACTIVE' 인 메뉴 모두 조회
        List<Menu> menus = menuRepository.findAllByStoreIdAndMenuStatusNot(
                storeId, MenuStatus.DELETED);

        if (menus==null||menus.isEmpty()) {
            throw new IllegalArgumentException("Menu not found");
        }

        menus.forEach(Menu::delete);
        menuRepository.saveAll(menus);
    }

}
