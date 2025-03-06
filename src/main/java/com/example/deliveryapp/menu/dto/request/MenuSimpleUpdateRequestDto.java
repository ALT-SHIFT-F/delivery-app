package com.example.deliveryapp.menu.dto.request;

import com.example.deliveryapp.menu.enums.MenuStatus;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuSimpleUpdateRequestDto {

    private MenuStatus menuStatus;

    @Min(value = 0, message = "재고 수량은 0이상이어야 합니다.")
    private Integer stockQuantity;
}
