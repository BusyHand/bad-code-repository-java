package com.example.couriermanagement.repository;

import com.example.couriermanagement.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    @Query("""
                SELECT v FROM Vehicle v 
                WHERE v.maxWeight >= :minWeight AND v.maxVolume >= :minVolume
            """)
    List<Vehicle> findByMinCapacity(
            @Param("minWeight") BigDecimal minWeight,
            @Param("minVolume") BigDecimal minVolume
    );

    @Query("""
                SELECT v FROM Vehicle v 
                WHERE v.id NOT IN (
                    SELECT d.vehicle.id FROM Delivery d 
                    WHERE d.deliveryDate = :date AND d.vehicle IS NOT NULL
                )
            """)
    List<Vehicle> findAvailableVehiclesForDate(@Param("date") LocalDate date);

    boolean existsByLicensePlate(String licensePlate);
}