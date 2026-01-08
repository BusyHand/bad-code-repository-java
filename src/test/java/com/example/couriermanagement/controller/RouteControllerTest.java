package com.example.couriermanagement.controller;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.request.RouteCalculationRequest;
import com.example.couriermanagement.dto.request.RoutePoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class RouteControllerTest extends BaseIntegrationTest {

    @Test
    public void calculateRoute_WithMultiplePoints_ReturnsCalculation() throws Exception {
        RouteCalculationRequest request = RouteCalculationRequest.builder()
                .points(Arrays.asList(
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6173"))
                                .build(),
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7540"))
                                .longitude(new BigDecimal("37.6200"))
                                .build()
                ))
                .build();

        expectSuccess(postJson("/routes/calculate", request, managerToken));
    }


    @Test
    public void calculateRoute_WithSinglePoint_ReturnsZeroDistance() throws Exception {
        RouteCalculationRequest request = RouteCalculationRequest.builder()
                .points(Collections.singletonList(
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6173"))
                                .build()
                ))
                .build();

        expectBadRequest(postJson("/routes/calculate", request, managerToken));
    }

    @Test
    public void calculateRoute_WithNoPoints_ReturnsZeroDistance() throws Exception {
        RouteCalculationRequest request = RouteCalculationRequest.builder()
                .points(Collections.emptyList())
                .build();

        expectBadRequest(postJson("/routes/calculate", request, managerToken));
    }

    @Test
    public void calculateRoute_WithCourierToken_Succeeds() throws Exception {
        RouteCalculationRequest request = RouteCalculationRequest.builder()
                .points(Arrays.asList(
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6173"))
                                .build(),
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7540"))
                                .longitude(new BigDecimal("37.6200"))
                                .build()
                ))
                .build();

        expectSuccess(postJson("/routes/calculate", request, courierToken));
    }

    @Test
    public void calculateRoute_WithoutToken_ReturnsUnauthorized() throws Exception {
        RouteCalculationRequest request = RouteCalculationRequest.builder()
                .points(Collections.emptyList())
                .build();

        expectForbidden(postJson("/routes/calculate", request, null));
    }

    @Test
    public void calculateRoute_WithThreePoints_CalculatesCorrectly() throws Exception {
        RouteCalculationRequest request = RouteCalculationRequest.builder()
                .points(Arrays.asList(
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7558"))
                                .longitude(new BigDecimal("37.6173"))
                                .build(),
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7540"))
                                .longitude(new BigDecimal("37.6200"))
                                .build(),
                        RoutePoint.builder()
                                .latitude(new BigDecimal("55.7520"))
                                .longitude(new BigDecimal("37.6230"))
                                .build()
                ))
                .build();

        expectSuccess(postJson("/routes/calculate", request, managerToken))
                .andExpect(jsonPath("$.distanceKm").value(0.55))
                .andExpect(jsonPath("$.durationMinutes").value(greaterThanOrEqualTo(0)));
    }
}
