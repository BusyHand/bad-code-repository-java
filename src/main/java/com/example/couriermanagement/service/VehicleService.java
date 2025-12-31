package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.VehicleDto;
import com.example.couriermanagement.dto.request.VehicleRequest;

import java.util.List;

public interface VehicleService {
    List<VehicleDto> getAllVehicles();

    VehicleDto createVehicle(VehicleRequest vehicleRequest);

    VehicleDto updateVehicle(Long id, VehicleRequest vehicleRequest);

    void deleteVehicle(Long id);
}