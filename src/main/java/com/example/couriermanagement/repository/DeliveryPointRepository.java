package com.example.couriermanagement.repository;

import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPointRepository extends JpaRepository<DeliveryPoint, Long> {

    List<DeliveryPoint> findByDeliveryOrderBySequence(Delivery delivery);

    List<DeliveryPoint> findByDeliveryId(Long deliveryId);

    void deleteByDeliveryId(Long deliveryId);

    @Query("""
                SELECT dp FROM DeliveryPoint dp 
                WHERE dp.delivery.id = :deliveryId 
                ORDER BY dp.sequence
            """)
    List<DeliveryPoint> findByDeliveryIdOrderBySequence(@Param("deliveryId") Long deliveryId);

    @Query("""
                SELECT MAX(dp.sequence) FROM DeliveryPoint dp 
                WHERE dp.delivery.id = :deliveryId
            """)
    Optional<Integer> findMaxSequenceByDeliveryId(@Param("deliveryId") Long deliveryId);

    Optional<DeliveryPoint> findByDeliveryAndSequence(Delivery delivery, Integer sequence);
}