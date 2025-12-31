package com.example.couriermanagement.service;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.request.DeliveryPointRequest;
import com.example.couriermanagement.dto.request.DeliveryProductRequest;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.entity.Product;
import com.example.couriermanagement.entity.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouteTimeValidationTest extends BaseIntegrationTest {

    @Autowired
    private DeliveryService deliveryService;

    @Test
    public void openStreetMapServiceMockShouldWorkForLongDistanceRoute() {
        BigDecimal distance = openStreetMapService.calculateDistance(
                new BigDecimal("55.7558"), // Moscow
                new BigDecimal("37.6176"),
                new BigDecimal("59.9311"), // St. Petersburg
                new BigDecimal("30.3609")
        );

        // Should return our mocked value
        assertEquals(new BigDecimal("635.0"), distance,
                "Expected 635.0 km, but got " + distance);

        System.out.println("✅ Long distance mock works: " + distance + " km");
    }

    @Test
    public void openStreetMapServiceMockShouldWorkForShortDistanceRoute() {
        BigDecimal distance = openStreetMapService.calculateDistance(
                new BigDecimal("55.7558"), // Moscow center
                new BigDecimal("37.6176"),
                new BigDecimal("55.7600"), // Moscow nearby
                new BigDecimal("37.6200")
        );

        // Should return our mocked value
        assertEquals(new BigDecimal("2.5"), distance,
                "Expected 2.5 km, but got " + distance);

        System.out.println("✅ Short distance mock works: " + distance + " km");
    }

    @Test
    public void deliveryValidationShouldPassForShortRouteWithSufficientTime() {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

        // Create delivery request with short route and sufficient time (9 hours)
        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(LocalDate.now().plusDays(5))
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(18, 0)) // 9 hours
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558")) // Moscow center
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(product.getId())
                                                .quantity(1)
                                                .build()
                                ))
                                .build(),
                        DeliveryPointRequest.builder()
                                .sequence(2)
                                .latitude(new BigDecimal("55.7600")) // Moscow nearby (~2.5 km)
                                .longitude(new BigDecimal("37.6200"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(product.getId())
                                                .quantity(1)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // This should succeed - short route with plenty of time
        try {
            // Установить контекст авторизации для manager-а (только manager может создавать доставки)
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            DeliveryDto result = deliveryService.createDelivery(deliveryRequest);
            System.out.println("✅ Short route delivery created successfully: " + result.getId());
        } catch (Exception e) {
            throw new AssertionError("Expected delivery creation to succeed for short route, but got: " + e.getMessage());
        } finally {
            // Очистить контекст после теста
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deliveryValidationShouldFailForLongRouteWithInsufficientTime() {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

        // Create delivery request with long route and insufficient time (30 minutes)
        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                .courierId(courierUser.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(LocalDate.now().plusDays(5))
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(9, 30)) // Only 30 minutes
                .points(Arrays.asList(
                        DeliveryPointRequest.builder()
                                .sequence(1)
                                .latitude(new BigDecimal("55.7558")) // Moscow
                                .longitude(new BigDecimal("37.6176"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(product.getId())
                                                .quantity(1)
                                                .build()
                                ))
                                .build(),
                        DeliveryPointRequest.builder()
                                .sequence(2)
                                .latitude(new BigDecimal("59.9311")) // St. Petersburg (~635 km)
                                .longitude(new BigDecimal("30.3609"))
                                .products(Arrays.asList(
                                        DeliveryProductRequest.builder()
                                                .productId(product.getId())
                                                .quantity(1)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // This should fail - long route with insufficient time
        try {
            // Установить контекст авторизации для manager-а (только manager может создавать доставки)
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerUser.getLogin(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            deliveryService.createDelivery(deliveryRequest);
            throw new AssertionError("Expected delivery creation to fail for long route with insufficient time");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Long route delivery correctly rejected: " + e.getMessage());
            assertTrue(e.getMessage().contains("Недостаточно времени"),
                    "Expected time validation error, but got: " + e.getMessage());
        } finally {
            // Очистить контекст после теста
            SecurityContextHolder.clearContext();
        }
    }
}