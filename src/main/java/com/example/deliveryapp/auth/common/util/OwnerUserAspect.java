package com.example.deliveryapp.auth.common.util;

import com.example.deliveryapp.auth.entity.AuthUser;
import com.example.deliveryapp.user.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OwnerUserAspect {

    private final AuthUserProvider authUserProvider;
    private final HttpServletRequest request;

    @Around("@annotation(com.example.deliveryapp.common.annotation.OwnerUser)")
    public Object checkOwner(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("유저 역활 OWNER인지 체크");

        // HTTP 요청에서 Authorization 헤더의 토큰 값을 가져옴
        String token = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 "Bearer "로 시작하지 않는 경우 예외 처리
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization 헤더가 없거나 잘못된 형식입니다.");
        }

        // "Bearer "를 제외한 실제 토큰 값만 추출
        String jwtToken = token.substring(7);

        // 토큰을 이용해 AuthUser 객체를 얻어오기
        AuthUser authUser = authUserProvider.getAuthUser(jwtToken); // 토큰을 전달하여 AuthUser를 가져옴

        // authUser가 null이 아닌지 확인
        if (authUser == null) {
            throw new IllegalStateException("로그인된 유저 정보가 없습니다.");
        }

        // authUser의 권한 체크
        if (authUser.getUserRole() != UserRole.ROLE_OWNER) {
            throw new AccessDeniedException("ROLE_OWNER 유저만 접근 가능합니다.");
        }

        return joinPoint.proceed();
    }
}
