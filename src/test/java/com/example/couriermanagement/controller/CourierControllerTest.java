package com.example.couriermanagement.controller;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.entity.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CourierControllerTest extends BaseIntegrationTest {

    @Test
    public void getCourierDeliveriesShouldReturnOwnDeliveries() throws Exception {
        createDelivery(courierUser, createVehicle());

        expectSuccess(getWithAuth("/courier/deliveries", courierToken))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deliveryNumber").exists())
                .andExpect(jsonPath("$[0].pointsCount").value(1))
                .andExpect(jsonPath("$[0].productsCount").value(2));
    }

    @Test
    public void getCourierDeliveriesAsAdminShouldReturn403() throws Exception {
        createDelivery(courierUser, createVehicle());

        expectForbidden(getWithAuth("/courier/deliveries", adminToken));
    }

    @Test
    public void getCourierDeliveriesAsManagerShouldReturn403() throws Exception {
        createDelivery(courierUser, createVehicle());

        expectForbidden(getWithAuth("/courier/deliveries", managerToken));
    }

    @Test
    public void getCourierDeliveriesWithoutAuthShouldReturn403() throws Exception {
        expectForbidden(mockMvc.perform(get("/courier/deliveries")));
    }

    @Test
    public void getCourierDeliveriesWithDateFilterShouldReturnFilteredResults() throws Exception {
        Delivery delivery = createDelivery(courierUser, createVehicle());
        LocalDate deliveryDate = delivery.getDeliveryDate();

        expectSuccess(getWithAuth("/courier/deliveries?date=" + deliveryDate, courierToken))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getCourierDeliveriesWithNonMatchingDateFilterShouldReturnEmpty() throws Exception {
        createDelivery(courierUser, createVehicle());
        LocalDate differentDate = LocalDate.now().plusDays(10);

        expectSuccess(getWithAuth("/courier/deliveries?date=" + differentDate, courierToken))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void getCourierDeliveriesWithStatusFilterShouldReturnFilteredResults() throws Exception {
        createDelivery(courierUser, createVehicle());

        expectSuccess(getWithAuth("/courier/deliveries?status=PLANNED", courierToken))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));
    }

    @Test
    public void getCourierDeliveriesWithDateRangeShouldReturnFilteredResults() throws Exception {
        Delivery delivery = createDelivery(courierUser, createVehicle());
        LocalDate deliveryDate = delivery.getDeliveryDate();
        LocalDate dateFrom = deliveryDate.minusDays(1);
        LocalDate dateTo = deliveryDate.plusDays(1);

        expectSuccess(getWithAuth("/courier/deliveries?date_from=" + dateFrom + "&date_to=" + dateTo, courierToken))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getCourierDeliveriesShouldNotReturnOtherCourierDeliveries() throws Exception {
        // Create delivery for another courier
        User anotherCourier = userRepository.save(
                User.builder()
                        .login("othercourier")
                        .passwordHash(passwordEncoder.encode("password"))
                        .name("Другой Курьер")
                        .role(UserRole.COURIER)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        createDelivery(anotherCourier, createVehicle());

        // Current courier should see no deliveries
        expectSuccess(getWithAuth("/courier/deliveries", courierToken))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void getCourierDeliveryByIdShouldReturnDeliveryDetails() throws Exception {
        Delivery delivery = createDelivery(courierUser, createVehicle());

        expectSuccess(getWithAuth("/courier/deliveries/" + delivery.getId(), courierToken))
                .andExpect(jsonPath("$.id").value(delivery.getId()))
                .andExpect(jsonPath("$.deliveryNumber").exists())
                .andExpect(jsonPath("$.courier.id").value(courierUser.getId()))
                .andExpect(jsonPath("$.deliveryPoints").isArray())
                .andExpect(jsonPath("$.deliveryPoints.length()").value(1))
                .andExpect(jsonPath("$.deliveryPoints[0].products").isArray());
    }

    @Test
    public void getOtherCourierDeliveryByIdShouldReturn403() throws Exception {
        // Create delivery for another courier
        User anotherCourier = userRepository.save(
                User.builder()
                        .login("othercourier2")
                        .passwordHash(passwordEncoder.encode("password"))
                        .name("Другой Курьер 2")
                        .role(UserRole.COURIER)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        Delivery delivery = createDelivery(anotherCourier, createVehicle());

        expectForbidden(getWithAuth("/courier/deliveries/" + delivery.getId(), courierToken)); // Service throws IllegalArgumentException about access
    }

    @Test
    public void getNonExistentDeliveryShouldReturn404() throws Exception {
        expectBadRequest(getWithAuth("/courier/deliveries/999", courierToken)); // Service throws IllegalArgumentException
    }

    @Test
    public void getCourierDeliveryAsAdminShouldReturn403() throws Exception {
        Delivery delivery = createDelivery(courierUser, createVehicle());

        expectForbidden(getWithAuth("/courier/deliveries/" + delivery.getId(), adminToken));
    }

    @Test
    public void getCourierDeliveryAsManagerShouldReturn403() throws Exception {
        Delivery delivery = createDelivery(courierUser, createVehicle());

        expectForbidden(getWithAuth("/courier/deliveries/" + delivery.getId(), managerToken));
    }

    @Test
    public void courierShouldSeeCorrectVehicleInformation() throws Exception {
        Vehicle vehicle = createVehicle();
        createDelivery(courierUser, vehicle);

        expectSuccess(getWithAuth("/courier/deliveries", courierToken))
                .andExpect(jsonPath("$[0].vehicle.brand").value("Ford Transit"))
                .andExpect(jsonPath("$[0].vehicle.licensePlate").value("А123БВ"));
    }

    @Test
    public void courierShouldSeeDeliveryWithNoVehicleAssigned() throws Exception {
        Delivery delivery = deliveryRepository.save(
                Delivery.builder()
                        .courier(courierUser)
                        .vehicle(null) // No vehicle assigned
                        .createdBy(managerUser)
                        .deliveryDate(LocalDate.now().plusDays(5))
                        .timeStart(LocalTime.of(9, 0))
                        .timeEnd(LocalTime.of(18, 0))
                        .status(DeliveryStatus.PLANNED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        expectSuccess(getWithAuth("/courier/deliveries", courierToken))
                .andExpect(jsonPath("$[0].vehicle.brand").value("Не назначена"))
                .andExpect(jsonPath("$[0].vehicle.licensePlate").value(""));
    }
}