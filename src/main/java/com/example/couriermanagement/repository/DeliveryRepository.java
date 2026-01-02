package com.example.couriermanagement.repository;

import com.example.couriermanagement.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {

    List<Delivery> findByDeliveryDate(LocalDate deliveryDate);

    List<Delivery> findByCourier(User courier);

    List<Delivery> findByCourierId(Long courierId);

    List<Delivery> findByCourierAndDeliveryDate(User courier, LocalDate deliveryDate);

    List<Delivery> findByDeliveryDateAndCourierId(LocalDate deliveryDate, Long courierId);

    List<Delivery> findByDeliveryDateAndCourierIdAndStatus(LocalDate deliveryDate, Long courierId, DeliveryStatus status);

    List<Delivery> findByDeliveryDateAndStatus(LocalDate deliveryDate, DeliveryStatus status);

    List<Delivery> findByCourierIdAndStatus(Long courierId, DeliveryStatus status);

    List<Delivery> findByCourierIdAndDeliveryDateBetween(Long courierId, LocalDate dateFrom, LocalDate dateTo);

    List<Delivery> findByCourierIdAndStatusAndDeliveryDateBetween(Long courierId, DeliveryStatus status, LocalDate dateFrom, LocalDate dateTo);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByVehicleId(Long vehicleId);

    @Query("""
                SELECT d FROM Delivery d 
                WHERE d.courier.id = :courierId 
                AND d.deliveryDate = :date 
                AND d.status IN ('PLANNED', 'IN_PROGRESS')
            """)
    List<Delivery> findActiveByCourierAndDate(
            @Param("courierId") Long courierId,
            @Param("date") LocalDate date
    );

    @Query("""
                SELECT d FROM Delivery d 
                WHERE d.deliveryDate = :date 
                AND d.status != 'CANCELLED'
                ORDER BY d.timeStart
            """)
    List<Delivery> findByDateOrderByTime(@Param("date") LocalDate date);

    @Query("""
                SELECT d FROM Delivery d 
                WHERE d.deliveryDate BETWEEN :startDate AND :endDate
                AND (:courierId IS NULL OR d.courier.id = :courierId)
                AND (:status IS NULL OR d.status = :status)
                ORDER BY d.deliveryDate, d.timeStart
            """)
    List<Delivery> findByDateRangeAndFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("courierId") Long courierId,
            @Param("status") DeliveryStatus status
    );

    @Query("""
                SELECT COUNT(d) > 0 FROM Delivery d 
                WHERE d.courier.id = :courierId 
                AND d.deliveryDate = :date
                AND d.status != 'CANCELLED'
                AND (
                    (d.timeStart <= :timeStart AND d.timeEnd > :timeStart) OR
                    (d.timeStart < :timeEnd AND d.timeEnd >= :timeEnd) OR
                    (d.timeStart >= :timeStart AND d.timeEnd <= :timeEnd)
                )
            """)
    boolean existsCourierTimeConflict(
            @Param("courierId") Long courierId,
            @Param("date") LocalDate date,
            @Param("timeStart") LocalTime timeStart,
            @Param("timeEnd") LocalTime timeEnd
    );

    @Query("""
                SELECT DISTINCT d FROM Delivery d 
                LEFT JOIN FETCH d.deliveryPoints
                LEFT JOIN FETCH d.vehicle
                WHERE d.courier.id = :courierId
            """)
    List<Delivery> findByCourierIdWithDetails(@Param("courierId") Long courierId);

    @Query("""
                SELECT DISTINCT d FROM Delivery d 
                LEFT JOIN FETCH d.deliveryPoints
                LEFT JOIN FETCH d.vehicle
                WHERE d.courier.id = :courierId 
                AND d.deliveryDate = :date
            """)
    List<Delivery> findByDeliveryDateAndCourierIdWithDetails(
            @Param("date") LocalDate date,
            @Param("courierId") Long courierId
    );

    @Query("""
                SELECT DISTINCT d FROM Delivery d 
                LEFT JOIN FETCH d.deliveryPoints
                LEFT JOIN FETCH d.vehicle
                WHERE d.courier.id = :courierId 
                AND d.status = :status
            """)
    List<Delivery> findByCourierIdAndStatusWithDetails(
            @Param("courierId") Long courierId,
            @Param("status") DeliveryStatus status
    );

    @Query("""
                SELECT DISTINCT d FROM Delivery d 
                LEFT JOIN FETCH d.deliveryPoints
                LEFT JOIN FETCH d.vehicle
                WHERE d.courier.id = :courierId 
                AND d.deliveryDate = :date 
                AND d.status = :status
            """)
    List<Delivery> findByDeliveryDateAndCourierIdAndStatusWithDetails(
            @Param("date") LocalDate date,
            @Param("courierId") Long courierId,
            @Param("status") DeliveryStatus status
    );

    @Query("""
                SELECT DISTINCT d FROM Delivery d 
                LEFT JOIN FETCH d.deliveryPoints
                LEFT JOIN FETCH d.vehicle
                WHERE d.courier.id = :courierId 
                AND d.deliveryDate BETWEEN :dateFrom AND :dateTo
            """)
    List<Delivery> findByCourierIdAndDeliveryDateBetweenWithDetails(
            @Param("courierId") Long courierId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
                SELECT DISTINCT d FROM Delivery d 
                LEFT JOIN FETCH d.deliveryPoints
                LEFT JOIN FETCH d.vehicle
                WHERE d.courier.id = :courierId 
                AND d.status = :status
                AND d.deliveryDate BETWEEN :dateFrom AND :dateTo
            """)
    List<Delivery> findByCourierIdAndStatusAndDeliveryDateBetweenWithDetails(
            @Param("courierId") Long courierId,
            @Param("status") DeliveryStatus status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

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
}