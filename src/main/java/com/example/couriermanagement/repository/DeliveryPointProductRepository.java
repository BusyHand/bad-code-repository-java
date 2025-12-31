package com.example.couriermanagement.repository;

import com.example.couriermanagement.entity.DeliveryPointProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryPointProductRepository extends JpaRepository<DeliveryPointProduct, Long> {
    List<DeliveryPointProduct> findByDeliveryPointId(Long deliveryPointId);

    List<DeliveryPointProduct> findByProductId(Long productId);

    void deleteByDeliveryPointId(Long deliveryPointId);
}