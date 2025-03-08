package com.example.deliveryapp.review.repository;

import com.example.deliveryapp.review.entity.Review;
import com.example.deliveryapp.store.entity.Store;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    void deleteByStore(Store store);


}
