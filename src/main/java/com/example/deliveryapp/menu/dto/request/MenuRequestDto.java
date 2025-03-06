package com.example.deliveryapp.menu.dto.request;

import com.example.deliveryapp.common.annotation.EnumCheck;
import com.example.deliveryapp.menu.enums.MenuCategory;
import com.example.deliveryapp.menu.enums.MenuStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class MenuRequestDto {

    @NotBlank(message = "메뉴 이름은 필수 입력사항입니다.")
    @Size(max = 20, message = "메뉴 이름은 최대 20자까지 입력 가능합니다.")
    private String menuName;

    @NotNull(message = "메뉴 가격은 필수 입력사항입니다.")
    @Min(value = 0,message = "메뉴 가격은 0이상어야 합니다.")
    private BigDecimal price;

//    @EnumCheck(enumClass = MenuCategory.class, message = "유효하지 않은 메뉴 카테고리입니다.")
    private MenuCategory menuCategory;

    @NotNull(message = "메뉴 상태는 필수 선택사항입니다.")
//    @EnumCheck(enumClass = MenuStatus.class, message = "유효하지 않은 메뉴 상태입니다.")
    private MenuStatus menuStatus;

    private Integer stockQuantity;

    private String description;
}
