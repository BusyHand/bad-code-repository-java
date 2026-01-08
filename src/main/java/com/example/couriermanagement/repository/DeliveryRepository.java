package com.example.couriermanagement.repository;

import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryPoint;
import com.example.couriermanagement.entity.DeliveryPointProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {

    List<Delivery> findByCourierId(Long courierId);

    List<Delivery> findByVehicleId(Long vehicleId);

    @Query("""
                SELECT DISTINCT dp FROM DeliveryPoint dp
                LEFT JOIN FETCH dp.deliveryPointProducts dpp
                LEFT JOIN FETCH dpp.product p
                WHERE dp.delivery IN :deliveries
                ORDER BY dp.sequence
            """)
    List<DeliveryPoint> loadDeliveryPoint(@Param("deliveries") List<Delivery> deliveries);

    @Query("""
                SELECT DISTINCT dpp FROM DeliveryPointProduct dpp
                LEFT JOIN FETCH dpp.product
                LEFT JOIN FETCH dpp.product p
                WHERE dpp.deliveryPoint IN :deliveryPoints
            """)
    List<DeliveryPointProduct> loadDeliveryPointsProductsByDeliveryPoint(@Param("deliveryPoints") List<DeliveryPoint> deliveryPoints);

    @Query("""
                SELECT d FROM Delivery d 
                WHERE d.deliveryDate = :date 
                AND d.vehicle.id = :vehicleId
                AND d.status NOT IN ('CANCELLED', 'COMPLETED')
                AND (
                    (d.timeStart <= :timeStart AND d.timeEnd > :timeStart) OR
                    (d.timeStart < :timeEnd AND d.timeEnd >= :timeEnd) OR
                    (d.timeStart >= :timeStart AND d.timeEnd <= :timeEnd)
                )
            """)
    List<Delivery> findByDateVehicleAndOverlappingTime(
            @Param("date") LocalDate date,
            @Param("vehicleId") Long vehicleId,
            @Param("timeStart") LocalTime timeStart,
            @Param("timeEnd") LocalTime timeEnd
    );

    @Query("""
                SELECT d FROM Delivery d 
                JOIN d.deliveryPoints dp 
                JOIN dp.deliveryPointProducts dpp 
                WHERE dpp.product.id = :productId
            """)
    List<Delivery> findByProductId(@Param("productId") Long productId);

    @EntityGraph(attributePaths = {
            "deliveryPoints"
    })
    Optional<Delivery> findWithPointsById(Long id);

    @Query("""
                SELECT dp FROM DeliveryPoint dp
                LEFT JOIN FETCH dp.deliveryPointProducts dpp
                LEFT JOIN FETCH dpp.product
                WHERE dp.delivery.id = :deliveryId
                ORDER BY dp.sequence
            """)
    List<DeliveryPoint> findPointsWithProducts(@Param("deliveryId") Long deliveryId);


}