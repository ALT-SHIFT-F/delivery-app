package com.example.deliveryapp.store.repository;

import com.example.deliveryapp.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // 특정 사용자의 가게 수를 조회
    int countByOwnerId(Long ownerId);

    // 특정 사용자가 소유한 가게 목록을 조회
    List<Store> findByOwnerId(Long ownerId);

    //특정 가게 조회
    @Query("SELECT s FROM Store s WHERE s.businessName LIKE %:keyword%")
    List<Store> findByBusinessNameContaining(@Param("keyword") String keyword);

}


