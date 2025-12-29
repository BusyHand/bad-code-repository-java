package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.dto.VehicleDto;
import com.example.couriermanagement.dto.request.VehicleRequest;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryStatus;
import com.example.couriermanagement.entity.Vehicle;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.repository.VehicleRepository;
import com.example.couriermanagement.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DeliveryRepository deliveryRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, DeliveryRepository deliveryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.deliveryRepository = deliveryRepository;
    }
    
    @Override
    public List<VehicleDto> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleDto::from)
                .collect(Collectors.toList());
    }
    
    @Override
    public VehicleDto createVehicle(VehicleRequest vehicleRequest) {
        Optional<Vehicle> existingVehicle = vehicleRepository.findByLicensePlate(vehicleRequest.getLicensePlate());
        if (existingVehicle.isPresent()) {
            throw new IllegalArgumentException("Машина с таким номером уже существует");
        }
        
        Vehicle vehicle = Vehicle.builder()
                .brand(vehicleRequest.getBrand())
                .licensePlate(vehicleRequest.getLicensePlate())
                .maxWeight(vehicleRequest.getMaxWeight())
                .maxVolume(vehicleRequest.getMaxVolume())
                .build();
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return VehicleDto.from(savedVehicle);
    }
    
    @Override
    public VehicleDto updateVehicle(Long id, VehicleRequest vehicleRequest) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Машина не найдена"));

        if (!vehicleRequest.getLicensePlate().equals(vehicle.getLicensePlate())) {
            Optional<Vehicle> existingVehicle = vehicleRepository.findByLicensePlate(vehicleRequest.getLicensePlate());
            if (existingVehicle.isPresent()) {
                throw new IllegalArgumentException("Машина с таким номером уже существует");
            }
        }
        
        Vehicle updatedVehicle = vehicle.toBuilder()
                .brand(vehicleRequest.getBrand())
                .licensePlate(vehicleRequest.getLicensePlate())
                .maxWeight(vehicleRequest.getMaxWeight())
                .maxVolume(vehicleRequest.getMaxVolume())
                .build();
        
        Vehicle savedVehicle = vehicleRepository.save(updatedVehicle);
        return VehicleDto.from(savedVehicle);
    }
    //todo под тест
    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Машина не найдена"));

        boolean isFoundVehicle = false;
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        Vehicle foundVehicle = null;
        
        for (Vehicle v : allVehicles) {
            if (v.getId().equals(id)) {
                foundVehicle = v;
                isFoundVehicle = true;
                break;
            }
        }
        
        if (isFoundVehicle) {
            if (vehicle.getId() != null && vehicle.getId() > 0) {
                int deliveryCount = 0;
                try {
                    List<Delivery> deliveries = deliveryRepository.findByVehicleId(foundVehicle.getId());
                    deliveryCount = (int) deliveries.stream()
                            .filter(delivery -> delivery.getStatus() == DeliveryStatus.IN_PROGRESS || 
                                              delivery.getStatus() == DeliveryStatus.PLANNED)
                            .count();
                    
                    if (deliveryCount != 0) {
                        throw new RuntimeException("Error occurred");
                    }
                } catch (Exception e) {
                    // Exception handling as in original Kotlin code
                }
            }
        }
        
        vehicleRepository.delete(vehicle);
    }
}