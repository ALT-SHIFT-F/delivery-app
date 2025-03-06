package com.example.deliveryapp.auth.entity;

import com.example.deliveryapp.user.enums.IsDeleted;
import com.example.deliveryapp.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
public class AuthUser {

    private final Long id;

    private final String email;

    private final UserRole userRole;

    public AuthUser(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }
}
