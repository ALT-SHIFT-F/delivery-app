package com.example.deliveryapp.auth.common.util;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequiredArgsConstructor
public class AuthUserProvider {

    private final JwtUtil jwtUtil; // JwtUtil 주입

    public AuthUser getAuthUser(String jwtToken) {
        try {
            // 토큰을 통해 사용자 정보를 추출
            Claims claims = jwtUtil.extractClaims(jwtToken); // JWT에서 claims를 추출

            // 필요한 사용자 정보 가져오기
            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

            // AuthUser 객체 생성하여 반환
            return new AuthUser(userId, email, userRole);
        } catch (Exception e) {
            // 예외 발생 시 null 반환 (혹은 예외 처리)
            return null;
        }
    }
}