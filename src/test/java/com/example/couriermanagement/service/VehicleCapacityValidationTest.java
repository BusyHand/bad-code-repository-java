package com.example.couriermanagement.service;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.request.DeliveryPointRequest;
import com.example.couriermanagement.dto.request.DeliveryProductRequest;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryStatus;
import com.example.couriermanagement.entity.Product;
import com.example.couriermanagement.entity.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VehicleCapacityValidationTest extends BaseIntegrationTest {

    @Autowired
    private DeliveryService deliveryService;

    private Product createHeavyProduct() {
        return productRepository.save(
                Product.builder()
                        .name("Тяжелый товар")
                        .weight(new BigDecimal("600.0")) // 600 кг
                        .length(new BigDecimal("100.0")) // 100 см
                        .width(new BigDecimal("100.0"))  // 100 см
                        .height(new BigDecimal("100.0"))  // 100 см = 1 м³
                        .build()
        );
    }

    private Product createBulkyProduct() {
        return productRepository.save(
                Product.builder()
                        .name("Объемный товар")
                        .weight(new BigDecimal("10.0"))   // 10 кг (легкий)
                        .length(new BigDecimal("200.0"))  // 200 см
                        .width(new BigDecimal("200.0"))   // 200 см
                        .height(new BigDecimal("200.0"))   // 200 см = 8 м³
                        .build()
        );
    }

    private Vehicle createSmallVehicle() {
        return vehicleRepository.save(
                Vehicle.builder()
                        .brand("Маленький грузовик")
                        .licensePlate("SMALL123")
                        .maxWeight(new BigDecimal("1000.0")) // 1 тонна
                        .maxVolume(new BigDecimal("10.0"))     // 10 м³
                        .build()
        );
    }

    @Test
    public void deliveryShouldSucceedWhenVehicleHasSufficientCapacity() {
        Vehicle vehicle = createSmallVehicle(); // 1000 кг, 10 м³
        Product lightProduct = createProduct(); // 1.5 кг, малый объем

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(LocalDate.now().plusDays(5))
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(18, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(lightProduct.getId())
                                                .quantity(10) // 10 * 1.5кг = 15кг - вполне в пределах лимита
                                                .build()
                                ))
                                .build()
                ))
                .build();

        try {
            // Установить контекст авторизации для manager-а
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto result = deliveryService.createDelivery(deliveryRequest);
            System.out.println("✅ Delivery created successfully with sufficient capacity: " + result.getId());
        } catch (Exception e) {
            throw new AssertionError("Expected delivery creation to succeed with sufficient capacity, but got: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deliveryShouldFailWhenExceedingVehicleWeightCapacity() {
        Vehicle vehicle = createSmallVehicle(); // 1000 кг, 10 м³
        Product heavyProduct = createHeavyProduct(); // 600 кг каждый

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(LocalDate.now().plusDays(5))
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(18, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(3) // 3 * 600кг = 1800кг > 1000кг (превышение веса)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        try {
            // Установить контекст авторизации для manager-а
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            deliveryService.createDelivery(deliveryRequest);
            throw new AssertionError("Expected delivery creation to fail due to weight capacity exceeded");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Delivery correctly rejected for weight exceeded: " + e.getMessage());
            assertTrue(e.getMessage().contains("Превышена грузоподъемность"),
                    "Expected weight capacity error, but got: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deliveryShouldFailWhenExceedingVehicleVolumeCapacity() {
        Vehicle vehicle = createSmallVehicle(); // 1000 кг, 10 м³
        Product bulkyProduct = createBulkyProduct(); // 10 кг, 8 м³ каждый

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(LocalDate.now().plusDays(5))
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(18, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(bulkyProduct.getId())
                                                .quantity(2) // 2 * 8м³ = 16м³ > 10м³ (превышение объема)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        try {
            // Установить контекст авторизации для manager-а
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            deliveryService.createDelivery(deliveryRequest);
            throw new AssertionError("Expected delivery creation to fail due to volume capacity exceeded");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Delivery correctly rejected for volume exceeded: " + e.getMessage());
            assertTrue(e.getMessage().contains("Превышен объем"),
                    "Expected volume capacity error, but got: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deliveryShouldFailWhenCombinedWithExistingDeliveriesExceedingCapacity() {
        Vehicle vehicle = createSmallVehicle(); // 1000 кг, 10 м³
        Product heavyProduct = createHeavyProduct(); // 600 кг каждый
        LocalDate date = LocalDate.now().plusDays(5);

        // Create first delivery with 600кг (within limit)
        DeliveryRequest firstDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date)
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(12, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // 1 * 600кг = 600кг
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // Create first delivery successfully
        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto firstDelivery = deliveryService.createDelivery(firstDeliveryRequest);
            System.out.println("✅ First delivery created: " + firstDelivery.getId() + " with 600кг");
        } catch (Exception e) {
            throw new AssertionError("First delivery should have succeeded: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }

        // Try to create second delivery with additional 1200кг (total = 1800кг > 1000кг)
        DeliveryRequest secondDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date) // Same date
                .timeStart(LocalTime.of(13, 0))
                .timeEnd(LocalTime.of(16, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7600"))
                                .longitude(new BigDecimal("37.6200"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // Another 600кг
                                                .build()
                                ))
                                .build(),
                        DeliveryPointRequest.builder()
                                .sequence(2)
                                .latitude(new BigDecimal("55.7700"))
                                .longitude(new BigDecimal("37.6300"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // Another 600кг, total = 1800кг - should fail
                                                .build()
                                ))
                                .build()
                ))
                .build();

        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            deliveryService.createDelivery(secondDeliveryRequest);
            throw new AssertionError("Expected second delivery to fail due to combined capacity exceeded");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Second delivery correctly rejected for combined capacity exceeded: " + e.getMessage());
            assertTrue(e.getMessage().contains("Превышена грузоподъемность"),
                    "Expected weight capacity error with existing deliveries, but got: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deliveryShouldSucceedWhenTimePeriodsDoNotOverlap() {
        Vehicle vehicle = createSmallVehicle(); // 1000 кг, 10 м³
        Product heavyProduct = createHeavyProduct(); // 600 кг каждый
        LocalDate date = LocalDate.now().plusDays(5);

        // Create first delivery with 600кг from 9:00-12:00
        DeliveryRequest firstDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date)
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(12, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // 1 * 600кг = 600кг
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // Create first delivery successfully
        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto firstDelivery = deliveryService.createDelivery(firstDeliveryRequest);
            System.out.println("✅ First delivery created: " + firstDelivery.getId() + " from 9:00-12:00 with 600кг");
        } catch (Exception e) {
            throw new AssertionError("First delivery should have succeeded: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }

        // Create second delivery with 600кг from 13:00-16:00 (no time overlap)
        DeliveryRequest secondDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date) // Same date
                .timeStart(LocalTime.of(13, 0)) // Different time - no overlap
                .timeEnd(LocalTime.of(16, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7600"))
                                .longitude(new BigDecimal("37.6200"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // Another 600кг - should succeed since no time overlap
                                                .build()
                                ))
                                .build()
                ))
                .build();

        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto secondDelivery = deliveryService.createDelivery(secondDeliveryRequest);
            System.out.println("✅ Second delivery created successfully: " + secondDelivery.getId() + " from 13:00-16:00 with 600кг (no time overlap)");
        } catch (Exception e) {
            throw new AssertionError("Second delivery should have succeeded with non-overlapping time, but got: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deliveryShouldFailWhenTimePeriodsOverlapAndExceedCapacity() {
        Vehicle vehicle = createSmallVehicle(); // 1000 кг, 10 м³
        Product heavyProduct = createHeavyProduct(); // 600 кг каждый
        LocalDate date = LocalDate.now().plusDays(5);

        // Create first delivery with 600кг from 9:00-13:00
        DeliveryRequest firstDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date)
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(13, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // 600кг
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // Create first delivery successfully
        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto firstDelivery = deliveryService.createDelivery(firstDeliveryRequest);
            System.out.println("✅ First delivery created: " + firstDelivery.getId() + " from 9:00-13:00 with 600кг");
        } catch (Exception e) {
            throw new AssertionError("First delivery should have succeeded: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }

        // Try to create overlapping delivery with 1200кг from 12:00-16:00 (overlaps 12:00-13:00)
        DeliveryRequest secondDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date) // Same date
                .timeStart(LocalTime.of(12, 0)) // Overlaps with first delivery (12:00-13:00)
                .timeEnd(LocalTime.of(16, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7600"))
                                .longitude(new BigDecimal("37.6200"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // 600кг + existing 600кг = 1200кг - should be close to limit
                                                .build()
                                ))
                                .build(),
                        DeliveryPointRequest.builder()
                                .sequence(2)
                                .latitude(new BigDecimal("55.7700"))
                                .longitude(new BigDecimal("37.6300"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // Another 600кг: total = 1800кг > 1000кг limit - should fail
                                                .build()
                                ))
                                .build()
                ))
                .build();

        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            deliveryService.createDelivery(secondDeliveryRequest);
            throw new AssertionError("Expected second delivery to fail due to overlapping time capacity exceeded");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Second delivery correctly rejected for overlapping time capacity exceeded: " + e.getMessage());
            assertTrue(e.getMessage().contains("Превышена грузоподъемность машины в период") ||
                            e.getMessage().contains("пересекающиеся доставки"),
                    "Expected time-specific capacity error, but got: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void completedDeliveriesShouldNotAffectCapacityValidation() {
        Vehicle vehicle = createSmallVehicle(); // 1000 кг, 10 м³
        Product heavyProduct = createHeavyProduct(); // 600 кг каждый
        LocalDate date = LocalDate.now().plusDays(5);

        // Create first delivery and mark it as completed
        DeliveryRequest firstDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date)
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(13, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // 600кг
                                                .build()
                                ))
                                .build()
                ))
                .build();

        Long firstDeliveryId;
        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto firstDelivery = deliveryService.createDelivery(firstDeliveryRequest);
            firstDeliveryId = firstDelivery.getId();
            System.out.println("✅ First delivery created: " + firstDelivery.getId() + " from 9:00-13:00 with 600кг");
        } catch (Exception e) {
            throw new AssertionError("First delivery should have succeeded: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }

        // Mark first delivery as completed (simulate via direct repository access)
        Delivery existingDelivery = deliveryRepository.findById(firstDeliveryId).get();
        Delivery completedDelivery = Delivery.builder()
                .id(existingDelivery.getId())
                .courier(existingDelivery.getCourier())
                .vehicle(existingDelivery.getVehicle())
                .createdBy(existingDelivery.getCreatedBy())
                .deliveryDate(existingDelivery.getDeliveryDate())
                .timeStart(existingDelivery.getTimeStart())
                .timeEnd(existingDelivery.getTimeEnd())
                .status(DeliveryStatus.COMPLETED)
                .createdAt(existingDelivery.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        deliveryRepository.save(completedDelivery);
        System.out.println("✅ Marked first delivery as completed");

        // Create overlapping delivery with 600кг from 12:00-16:00
        // Should succeed because completed delivery doesn't count toward capacity
        DeliveryRequest secondDeliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date)
                .timeStart(LocalTime.of(12, 0)) // Overlaps with completed delivery
                .timeEnd(LocalTime.of(16, 0))
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7600"))
                                .longitude(new BigDecimal("37.6200"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(heavyProduct.getId())
                                                .quantity(1) // 600кг - should succeed since completed delivery doesn't count
                                                .build()
                                ))
                                .build()
                ))
                .build();

        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto secondDelivery = deliveryService.createDelivery(secondDeliveryRequest);
            System.out.println("✅ Second delivery created successfully: " + secondDelivery.getId() + " - completed deliveries ignored");
        } catch (Exception e) {
            throw new AssertionError("Second delivery should have succeeded since completed deliveries don't count, but got: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}