package com.example.deliveryapp.menu.controller;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.common.annotation.Auth;
import com.example.deliveryapp.common.annotation.OwnerUser;
import com.example.deliveryapp.menu.dto.request.MenuRequestDto;
import com.example.deliveryapp.menu.dto.request.MenuSimpleUpdateRequestDto;
import com.example.deliveryapp.menu.dto.response.MenuResponseDto;
import com.example.deliveryapp.menu.dto.response.MenuSimpleResponseDto;
import com.example.deliveryapp.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @OwnerUser
    @PostMapping
    public ResponseEntity<MenuSimpleResponseDto> createMenu(
            @Auth AuthUser user,
            @RequestBody @Valid MenuRequestDto dto,
            @PathVariable Long storeId) {

        MenuSimpleResponseDto responseDto = menuService.saveMenu(dto, storeId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<MenuSimpleResponseDto>> getMenus(
            @PathVariable Long storeId) {

        List<MenuSimpleResponseDto> responseDtos = menuService.findAvailableMenusByStoreId(storeId);
        return ResponseEntity.ok(responseDtos);
    }

    @OwnerUser
    @GetMapping("/edit")
    public ResponseEntity<List<MenuResponseDto>> getMenusForEdit(
            @Auth AuthUser user,
            @PathVariable Long storeId) {

        List<MenuResponseDto> responseDtos = menuService.findAllMenusByStoreId(storeId);
        return ResponseEntity.ok(responseDtos);
    }

    @OwnerUser
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponseDto> updateMenu(
            @Auth AuthUser user,
            @PathVariable("menuId") Long menuId,
            @RequestBody @Valid MenuRequestDto dto) {

        MenuResponseDto responseDto = menuService.updateMenuDetail(dto, menuId);
        return ResponseEntity.ok(responseDto);
    }

    @OwnerUser
    @PatchMapping("/{menuId}")
    public ResponseEntity<MenuSimpleResponseDto> updateMenuLite(
            @Auth AuthUser user,
            @PathVariable("menuId") Long menuId,
            @RequestBody @Valid MenuSimpleUpdateRequestDto dto) {

        MenuSimpleResponseDto responseDto = menuService.updateMenuSimple(dto, menuId);
        return ResponseEntity.ok(responseDto);
    }

    @OwnerUser
    @DeleteMapping
    public ResponseEntity<Void> deleteAllMenu(
            @Auth AuthUser user,
            @PathVariable Long storeId) {

        menuService.deleteAll(storeId);
        return ResponseEntity.noContent().build();
    }

    @OwnerUser
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @Auth AuthUser user,
            @PathVariable Long menuId) {

        menuService.delete(menuId);
        return ResponseEntity.noContent().build();
    }
}
