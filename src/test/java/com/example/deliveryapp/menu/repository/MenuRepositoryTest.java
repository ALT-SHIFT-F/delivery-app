package com.example.deliveryapp.menu.repository;

import com.example.deliveryapp.config.JpaConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Import({JpaConfig.class})
class MenuRepositoryTest {
  
}