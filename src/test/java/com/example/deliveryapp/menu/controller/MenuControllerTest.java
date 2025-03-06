package com.example.deliveryapp.menu.controller;
import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.menu.dto.request.MenuRequestDto;
import com.example.deliveryapp.menu.dto.request.MenuSimpleUpdateRequestDto;
import com.example.deliveryapp.menu.dto.response.MenuResponseDto;
import com.example.deliveryapp.menu.dto.response.MenuSimpleResponseDto;
import com.example.deliveryapp.menu.enums.MenuCategory;
import com.example.deliveryapp.menu.enums.MenuStatus;
import com.example.deliveryapp.menu.service.MenuService;
import com.example.deliveryapp.user.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import java.util.List;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthUser authUser;
    private Long storeId;
    private MenuRequestDto menuRequestDto;

    // 공통 데이터 초기화
    @BeforeEach
    void 메뉴_컨트롤러_테스트준비() {
        // given
        authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_OWNER);  // 사장 유저 설정
        storeId = 1L;  // 테스트 스토어 ID 설정
        menuRequestDto = new MenuRequestDto(
                "후라이드_치킨",
                BigDecimal.valueOf(15000),
                MenuCategory.MAIN,
                MenuStatus.AVAILABLE,
                10,
                "바삭바삭한_후라이드_치킨"
        ); // 메뉴 생성용 RequestDto 설정
    }

    @Test
    void 메뉴_생성_성공() throws Exception {
        // given
        MenuSimpleResponseDto responseDto = new MenuSimpleResponseDto(1L, "후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, 10, "바삭바삭한_후라이드_치킨");

        // 메뉴 생성 서비스 메서드 호출 시 responseDto 반환 설정
        given(menuService.saveMenu(any(MenuRequestDto.class), eq(storeId), eq(authUser))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/stores/{storeId}/menus", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(menuRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.menuName").value("후라이드_치킨"))
                .andExpect(jsonPath("$.price").value(15000))
                .andExpect(jsonPath("$.menuCategory").value(MenuCategory.MAIN))
                .andExpect(jsonPath("$.menuStatus").value("AVAILABLE"));
    }

    @Test
    void 메뉴_목록_조회() throws Exception {
        // given
        MenuSimpleResponseDto responseDto = new MenuSimpleResponseDto(1L, "후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, 10, "바삭바삭한_후라이드_치킨");
        given(menuService.findAvailableMenusByStoreId(storeId)).willReturn(List.of(responseDto));

        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}/menus", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].menuName").value("후라이드_치킨"))
                .andExpect(jsonPath("$[0].price").value(15000))
                .andExpect(jsonPath("$[0].menuCategory").value(equalToIgnoringCase("MAIN")));
    }

    @Test
    void 메뉴_Detail_수정_성공() throws Exception {
        // given
        Long menuId = 1L;
        MenuRequestDto requestDto = new MenuRequestDto(
                "후라이드_치킨_수정",
                BigDecimal.valueOf(16000),
                MenuCategory.MAIN,
                MenuStatus.AVAILABLE,
                15,
                "바삭바삭한_후라이드_치킨_수정"
        );

        //업데이트 후 responseDto
        MenuResponseDto responseDto = new MenuResponseDto(
                1L,
                "후라이드_치킨_수정",
                BigDecimal.valueOf(16000),
                MenuCategory.MAIN,
                MenuStatus.AVAILABLE,
                15,
                10,  // 재고 수량
                "바삭바삭한_후라이드_치킨_수정",
                null,  // createdAt
                null   // updatedAt
        );

        // 서비스 메서드 호출 시 responseDto 반환 설정
        given(menuService.updateMenuDetail(any(MenuRequestDto.class), eq(menuId))).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/v1/stores/{storeId}/menus/{menuId}", storeId, menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuName").value("후라이드_치킨_수정"))
                .andExpect(jsonPath("$.price").value(16000))
                .andExpect(jsonPath("$.menuCategory").value(MenuCategory.MAIN.toString()))  // Enum 값을 string으로 변환하여 비교
                .andExpect(jsonPath("$.menuStatus").value(MenuStatus.AVAILABLE.toString()));  // Enum 값을 string으로 변환하여 비교
    }

    @Test
    void 메뉴_Simple_수정_성공() throws Exception {
        // given
        Long menuId = 1L;
        MenuSimpleUpdateRequestDto requestDto = new MenuSimpleUpdateRequestDto();
        ReflectionTestUtils.setField(requestDto, "menuStatus", MenuStatus.INACTIVE);
        ReflectionTestUtils.setField(requestDto, "stockQuantity", 20);

        MenuSimpleResponseDto responseDto = new MenuSimpleResponseDto(1L, "후라이드_치킨", BigDecimal.valueOf(15000), MenuCategory.MAIN, 20, "바삭바삭한_후라이드_치킨");

        // 메뉴 단순 업데이트 메서드 호출 시 responseDto 반환 설정
        given(menuService.updateMenuSimple(eq(requestDto), eq(menuId))).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/v1/stores/{storeId}/menus/{menuId}", storeId, menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuStatus").value(MenuStatus.INACTIVE))
                .andExpect(jsonPath("$.stockQuantity").value(20));
    }

    @Test
    void 메뉴_삭제_성공() throws Exception {
        // given
        long menuId = 1L;

        // 메뉴 삭제 메서드 호출 시 아무것도 반환하지 않음
        doNothing().when(menuService).delete(menuId);

        // when & then
        mockMvc.perform(delete("/api/v1/stores/{storeId}/menus/{menuId}", storeId, menuId))
                .andExpect(status().isNoContent());
    }
}