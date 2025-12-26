package com.example.couriermanagement.controller;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.request.*;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryStatus;
import com.example.couriermanagement.entity.Product;
import com.example.couriermanagement.entity.Vehicle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DeliveryControllerTest extends BaseIntegrationTest {

    @Test
    public void getAllDeliveriesAsManagerShouldSucceed() throws Exception {
        createDelivery();

        expectSuccess(getWithAuth("/deliveries", managerToken))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getAllDeliveriesAsCourierShouldReturn403() throws Exception {
        expectForbidden(getWithAuth("/deliveries", courierToken));
    }

    @Test
    public void getAllDeliveriesAsAdminShouldReturn403() throws Exception {
        expectForbidden(getWithAuth("/deliveries", adminToken));
    }

    @Test
    public void getDeliveriesWithDateFilterShouldReturnFilteredResults() throws Exception {
        Delivery delivery = createDelivery();
        LocalDate deliveryDate = delivery.getDeliveryDate();

        expectSuccess(getWithAuth("/deliveries?date=" + deliveryDate, managerToken))
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getDeliveriesWithCourierFilterShouldReturnFilteredResults() throws Exception {
        createDelivery();

        expectSuccess(getWithAuth("/deliveries?courier_id=" + courierUser.getId(), managerToken))
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getDeliveriesWithStatusFilterShouldReturnFilteredResults() throws Exception {
        createDelivery();

        expectSuccess(getWithAuth("/deliveries?status=PLANNED", managerToken))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("PLANNED"));
    }

    @Test
    public void createDeliveryAsManagerShouldSucceed() throws Exception {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

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
                            .productId(product.getId())
                            .quantity(3)
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectSuccess(postJson("/deliveries", deliveryRequest, managerToken))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.courier.id").value(courierUser.getId()))
            .andExpect(jsonPath("$.vehicle.id").value(vehicle.getId()))
            .andExpect(jsonPath("$.deliveryPoints.length()").value(1))
            .andExpect(jsonPath("$.deliveryPoints[0].products.length()").value(1));
    }

    @Test
    public void createDeliveryAsCourierShouldReturn403() throws Exception {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

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
                            .productId(product.getId())
                            .quantity(3)
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectForbidden(postJson("/deliveries", deliveryRequest, courierToken));
    }

    @Test
    public void createDeliveryWithInvalidCourierRoleShouldReturn400() throws Exception {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(adminUser.getId()) // Admin, not courier
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
                            .productId(product.getId())
                            .quantity(3)
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectBadRequest(postJson("/deliveries", deliveryRequest, managerToken));
    }

    @Test
    public void createDeliveryWithPastDateShouldReturn400() throws Exception {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(vehicle.getId())
            .deliveryDate(LocalDate.now().minusDays(1)) // Past date
            .timeStart(LocalTime.of(9, 0))
            .timeEnd(LocalTime.of(18, 0))
            .points(Arrays.asList(
                DeliveryPointRequest.builder()
                    .sequence(1)
                    .latitude(new BigDecimal("55.7558"))
                    .longitude(new BigDecimal("37.6176"))
                    .products(Arrays.asList(
                        DeliveryProductRequest.builder()
                            .productId(product.getId())
                            .quantity(3)
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectBadRequest(postJson("/deliveries", deliveryRequest, managerToken));
    }

    @Test
    public void createDeliveryWithInvalidTimeShouldReturn400() throws Exception {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(vehicle.getId())
            .deliveryDate(LocalDate.now().plusDays(5))
            .timeStart(LocalTime.of(18, 0)) // Start after end
            .timeEnd(LocalTime.of(9, 0))
            .points(Arrays.asList(
                DeliveryPointRequest.builder()
                    .sequence(1)
                    .latitude(new BigDecimal("55.7558"))
                    .longitude(new BigDecimal("37.6176"))
                    .products(Arrays.asList(
                        DeliveryProductRequest.builder()
                            .productId(product.getId())
                            .quantity(3)
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectBadRequest(postJson("/deliveries", deliveryRequest, managerToken));
    }

    @Test
    public void getDeliveryByIdShouldReturnDeliveryDetails() throws Exception {
        Delivery delivery = createDelivery();

        expectSuccess(getWithAuth("/deliveries/" + delivery.getId(), managerToken))
            .andExpect(jsonPath("$.id").value(delivery.getId()))
            .andExpect(jsonPath("$.deliveryPoints").isArray())
            .andExpect(jsonPath("$.canEdit").isBoolean());
    }

    @Test
    public void updateDeliveryAsManagerShouldSucceedWhenMoreThan3DaysBefore() throws Exception {
        Delivery delivery = createDelivery();
        Product product = createProduct();

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(delivery.getVehicle().getId())
            .deliveryDate(LocalDate.now().plusDays(5))
            .timeStart(LocalTime.of(10, 0)) // Changed time
            .timeEnd(LocalTime.of(19, 0))   // Changed time
            .points(Arrays.asList(
                DeliveryPointRequest.builder()
                    .sequence(1)
                    .latitude(new BigDecimal("55.7600")) // Changed coordinates
                    .longitude(new BigDecimal("37.6200"))
                    .products(Arrays.asList(
                        DeliveryProductRequest.builder()
                            .productId(product.getId())
                            .quantity(5) // Changed quantity
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectSuccess(putJson("/deliveries/" + delivery.getId(), deliveryRequest, managerToken))
            .andExpect(jsonPath("$.timeStart").value("10:00:00"))
            .andExpect(jsonPath("$.timeEnd").value("19:00:00"));
    }

    @Test
    public void updateDeliveryLessThan3DaysBeforeShouldReturn400() throws Exception {
        // Create delivery for tomorrow (less than 3 days)
        Vehicle vehicle = createVehicle();
        Delivery nearDelivery = deliveryRepository.save(
            Delivery.builder()
                .courier(courierUser)
                .vehicle(vehicle)
                .createdBy(managerUser)
                .deliveryDate(LocalDate.now().plusDays(1)) // Tomorrow
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(18, 0))
                .status(DeliveryStatus.PLANNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );

        Product product = createProduct();
        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(vehicle.getId())
            .deliveryDate(LocalDate.now().plusDays(5))
            .timeStart(LocalTime.of(10, 0))
            .timeEnd(LocalTime.of(19, 0))
            .points(Arrays.asList(
                DeliveryPointRequest.builder()
                    .sequence(1)
                    .latitude(new BigDecimal("55.7558"))
                    .longitude(new BigDecimal("37.6176"))
                    .products(Arrays.asList(
                        DeliveryProductRequest.builder()
                            .productId(product.getId())
                            .quantity(3)
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectBadRequest(putJson("/deliveries/" + nearDelivery.getId(), deliveryRequest, managerToken));
    }

    @Test
    public void deleteDeliveryAsManagerShouldSucceedWhenMoreThan3DaysBefore() throws Exception {
        Delivery delivery = createDelivery();

        deleteWithAuth("/deliveries/" + delivery.getId(), managerToken)
            .andExpect(status().isNoContent());
    }

    @Test
    public void deleteDeliveryLessThan3DaysBeforeShouldReturn400() throws Exception {
        Vehicle vehicle = createVehicle();
        Delivery nearDelivery = deliveryRepository.save(
            Delivery.builder()
                .courier(courierUser)
                .vehicle(vehicle)
                .createdBy(managerUser)
                .deliveryDate(LocalDate.now().plusDays(1)) // Tomorrow
                .timeStart(LocalTime.of(9, 0))
                .timeEnd(LocalTime.of(18, 0))
                .status(DeliveryStatus.PLANNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );

        expectBadRequest(deleteWithAuth("/deliveries/" + nearDelivery.getId(), managerToken));
    }

    @Test
    public void generateDeliveriesAsManagerShouldSucceed() throws Exception {
        Product product = createProduct();

        Map<LocalDate, java.util.List<RouteWithProducts>> deliveryData = new HashMap<>();
        deliveryData.put(LocalDate.now().plusDays(5), Arrays.asList(
            RouteWithProducts.builder()
                .route(Arrays.asList(
                    DeliveryPointRequest.builder()
                        .sequence(1)
                        .latitude(new BigDecimal("55.7558"))
                        .longitude(new BigDecimal("37.6176"))
                        .products(Collections.emptyList())
                        .build()
                ))
                .products(Arrays.asList(
                    DeliveryProductRequest.builder()
                        .productId(product.getId())
                        .quantity(5)
                        .build()
                ))
                .build()
        ));

        System.out.println("TESTING" + deliveryData);

        GenerateDeliveriesRequest generateRequest = GenerateDeliveriesRequest.builder()
            .deliveryData(deliveryData)
            .build();

        expectSuccess(postJson("/deliveries/generate", generateRequest, managerToken))
            .andExpect(jsonPath("$.totalGenerated").exists())
            .andExpect(jsonPath("$.byDate").exists());
    }

    @Test
    public void generateDeliveriesAsCourierShouldReturn403() throws Exception {
        Product product = createProduct();

        Map<LocalDate, java.util.List<RouteWithProducts>> deliveryData = new HashMap<>();
        deliveryData.put(LocalDate.now().plusDays(5), Arrays.asList(
            RouteWithProducts.builder()
                .route(Arrays.asList(
                    DeliveryPointRequest.builder()
                        .sequence(1)
                        .latitude(new BigDecimal("55.7558"))
                        .longitude(new BigDecimal("37.6176"))
                        .products(Collections.emptyList())
                        .build()
                ))
                .products(Arrays.asList(
                    DeliveryProductRequest.builder()
                        .productId(product.getId())
                        .quantity(5)
                        .build()
                ))
                .build()
        ));

        GenerateDeliveriesRequest generateRequest = GenerateDeliveriesRequest.builder()
            .deliveryData(deliveryData)
            .build();

        expectForbidden(postJson("/deliveries/generate", generateRequest, courierToken));
    }

    @Test
    public void createDeliveryWithInsufficientTimeWindowShouldReturn400() throws Exception {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(vehicle.getId())
            .deliveryDate(LocalDate.now().plusDays(5))
            .timeStart(LocalTime.of(9, 0))
            .timeEnd(LocalTime.of(9, 30)) // Only 30 minutes for long route
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
                    .latitude(new BigDecimal("59.9311")) // St. Petersburg - 635km away
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

        expectBadRequest(postJson("/deliveries", deliveryRequest, managerToken));
    }

    @Test
    public void updateDeliveryWithInsufficientTimeWindowShouldReturn400() throws Exception {
        Delivery delivery = createDelivery();
        Product product = createProduct();

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(delivery.getVehicle().getId())
            .deliveryDate(LocalDate.now().plusDays(5))
            .timeStart(LocalTime.of(10, 0))
            .timeEnd(LocalTime.of(10, 30)) // Only 30 minutes for long route
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
                    .latitude(new BigDecimal("59.9311")) // St. Petersburg - 635km away
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

        expectBadRequest(putJson("/deliveries/" + delivery.getId(), deliveryRequest, managerToken));
    }

    @Test
    public void createDeliveryWithSufficientTimeWindowShouldSucceed() throws Exception {
        Vehicle vehicle = createVehicle();
        Product product = createProduct();

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(vehicle.getId())
            .deliveryDate(LocalDate.now().plusDays(5))
            .timeStart(LocalTime.of(9, 0))
            .timeEnd(LocalTime.of(18, 0)) // 9 hours should be enough for short routes
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
                    .latitude(new BigDecimal("55.7600")) // Short distance within Moscow - ~2.5 km
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

        expectSuccess(postJson("/deliveries", deliveryRequest, managerToken))
            .andExpect(jsonPath("$.deliveryPoints.length()").value(2));
    }

    @Test
    public void createDeliveryShouldFailWhenVehicleWeightCapacityExceeded() throws Exception {
        // Create heavy product (500kg each)
        Product heavyProduct = productRepository.save(
            Product.builder()
                .name("Тяжелый товар")
                .weight(new BigDecimal("500.0"))
                .length(new BigDecimal("100.0"))
                .width(new BigDecimal("100.0"))
                .height(new BigDecimal("100.0"))
                .build()
        );
        
        // Create small vehicle (1000kg capacity)
        Vehicle smallVehicle = vehicleRepository.save(
            Vehicle.builder()
                .brand("Маленький грузовик")
                .licensePlate("SMALL456")
                .maxWeight(new BigDecimal("1000.0"))
                .maxVolume(new BigDecimal("10.0"))
                .build()
        );

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(smallVehicle.getId())
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
                            .quantity(3) // 3 * 500kg = 1500kg > 1000kg capacity
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectBadRequest(postJson("/deliveries", deliveryRequest, managerToken));
    }

    @Test
    public void createDeliveryShouldFailWhenVehicleVolumeCapacityExceeded() throws Exception {
        // Create bulky product (8 m³ each)
        Product bulkyProduct = productRepository.save(
            Product.builder()
                .name("Объемный товар")
                .weight(new BigDecimal("10.0"))
                .length(new BigDecimal("200.0")) // 200cm
                .width(new BigDecimal("200.0"))  // 200cm
                .height(new BigDecimal("200.0"))  // 200cm = 8 m³
                .build()
        );
        
        // Create small vehicle (10 m³ capacity)  
        Vehicle smallVehicle = vehicleRepository.save(
            Vehicle.builder()
                .brand("Маленький грузовик")
                .licensePlate("SMALL789")
                .maxWeight(new BigDecimal("2000.0"))
                .maxVolume(new BigDecimal("10.0")) // Only 10 m³
                .build()
        );

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
            .courierId(courierUser.getId())
            .vehicleId(smallVehicle.getId())
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
                            .quantity(2) // 2 * 8 m³ = 16 m³ > 10 m³ capacity
                            .build()
                    ))
                    .build()
            ))
            .build();

        expectBadRequest(postJson("/deliveries", deliveryRequest, managerToken));
    }
}