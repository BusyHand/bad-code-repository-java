package com.example.couriermanagement.controller;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.request.VehicleRequest;
import com.example.couriermanagement.entity.Vehicle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VehicleControllerTest extends BaseIntegrationTest {

    @Test
    public void getAllVehiclesShouldReturnListOfVehicles() throws Exception {
        createVehicle();

        expectSuccess(getWithAuth("/vehicles", adminToken))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].brand").value("Ford Transit"))
                .andExpect(jsonPath("$[0].licensePlate").value("А123БВ"));
    }

    @Test
    public void getAllVehiclesWithoutAuthShouldReturn403() throws Exception {
        expectForbidden(mockMvc.perform(get("/vehicles")));
    }

    @Test
    public void createVehicleAsAdminShouldSucceed() throws Exception {
        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Mercedes Sprinter")
                .licensePlate("В456ГД")
                .maxWeight(new BigDecimal("1500.0"))
                .maxVolume(new BigDecimal("20.0"))
                .build();

        expectSuccess(postJson("/vehicles", vehicleRequest, adminToken))
                .andExpect(jsonPath("$.brand").value("Mercedes Sprinter"))
                .andExpect(jsonPath("$.licensePlate").value("В456ГД"))
                .andExpect(jsonPath("$.maxWeight").value(1500.0))
                .andExpect(jsonPath("$.maxVolume").value(20.0));
    }

    @Test
    public void createVehicleAsManagerShouldReturn403() throws Exception {
        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Mercedes Sprinter")
                .licensePlate("В456ГД")
                .maxWeight(new BigDecimal("1500.0"))
                .maxVolume(new BigDecimal("20.0"))
                .build();

        expectForbidden(postJson("/vehicles", vehicleRequest, managerToken));
    }

    @Test
    public void createVehicleAsCourierShouldReturn403() throws Exception {
        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Mercedes Sprinter")
                .licensePlate("В456ГД")
                .maxWeight(new BigDecimal("1500.0"))
                .maxVolume(new BigDecimal("20.0"))
                .build();

        expectForbidden(postJson("/vehicles", vehicleRequest, courierToken));
    }

    @Test
    public void createVehicleWithDuplicateLicensePlateShouldReturn400() throws Exception {
        createVehicle(); // Creates vehicle with А123БВ

        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Mercedes Sprinter")
                .licensePlate("А123БВ") // Same license plate
                .maxWeight(new BigDecimal("1500.0"))
                .maxVolume(new BigDecimal("20.0"))
                .build();

        expectBadRequest(postJson("/vehicles", vehicleRequest, adminToken));
    }

    @Test
    public void createVehicleWithInvalidDataShouldReturn400() throws Exception {
        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("")
                .licensePlate("")
                .maxWeight(new BigDecimal("-100.0")) // Negative weight
                .maxVolume(new BigDecimal("-10.0"))   // Negative volume
                .build();

        expectBadRequest(postJson("/vehicles", vehicleRequest, adminToken));
    }

    @Test
    public void updateVehicleAsAdminShouldSucceed() throws Exception {
        Vehicle vehicle = createVehicle();

        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Updated Ford")
                .licensePlate("Г789ЕЖ")
                .maxWeight(new BigDecimal("2000.0"))
                .maxVolume(new BigDecimal("25.0"))
                .build();

        expectSuccess(putJson("/vehicles/" + vehicle.getId(), vehicleRequest, adminToken))
                .andExpect(jsonPath("$.brand").value("Updated Ford"))
                .andExpect(jsonPath("$.licensePlate").value("Г789ЕЖ"));
    }

    @Test
    public void updateVehicleAsManagerShouldReturn403() throws Exception {
        Vehicle vehicle = createVehicle();

        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Updated Ford")
                .licensePlate("Г789ЕЖ")
                .maxWeight(new BigDecimal("2000.0"))
                .maxVolume(new BigDecimal("25.0"))
                .build();

        expectForbidden(putJson("/vehicles/" + vehicle.getId(), vehicleRequest, managerToken));
    }

    @Test
    public void updateNonExistentVehicleShouldReturn404() throws Exception {
        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Updated Ford")
                .licensePlate("Г789ЕЖ")
                .maxWeight(new BigDecimal("2000.0"))
                .maxVolume(new BigDecimal("25.0"))
                .build();

        expectBadRequest(putJson("/vehicles/999", vehicleRequest, adminToken)); // Service throws IllegalArgumentException, which becomes 400
    }

    @Test
    public void updateVehicleWithDuplicateLicensePlateShouldReturn400() throws Exception {
        Vehicle vehicle1 = createVehicle();
        Vehicle vehicle2 = vehicleRepository.save(
                Vehicle.builder()
                        .brand("Mercedes")
                        .licensePlate("В456ГД")
                        .maxWeight(new BigDecimal("1500.0"))
                        .maxVolume(new BigDecimal("20.0"))
                        .build()
        );

        VehicleRequest vehicleRequest = VehicleRequest.builder()
                .brand("Updated Mercedes")
                .licensePlate("А123БВ") // Same as vehicle1
                .maxWeight(new BigDecimal("2000.0"))
                .maxVolume(new BigDecimal("25.0"))
                .build();

        expectBadRequest(putJson("/vehicles/" + vehicle2.getId(), vehicleRequest, adminToken));
    }

    @Test
    public void deleteVehicleAsAdminShouldSucceed() throws Exception {
        Vehicle vehicle = createVehicle();

        deleteWithAuth("/vehicles/" + vehicle.getId(), adminToken)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteVehicleAsManagerShouldReturn403() throws Exception {
        Vehicle vehicle = createVehicle();

        expectForbidden(deleteWithAuth("/vehicles/" + vehicle.getId(), managerToken));
    }

    @Test
    public void deleteNonExistentVehicleShouldReturn404() throws Exception {
        expectBadRequest(deleteWithAuth("/vehicles/999", adminToken)); // Service throws IllegalArgumentException, which becomes 400
    }
}