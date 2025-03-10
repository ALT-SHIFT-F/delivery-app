package com.example.deliveryapp.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangeUserNameRequestDto {

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$" ,
            message = "닉네임은 특수문자를 포함하지 않은 2~10자리여야 합니다.")
    private String userName;
}
