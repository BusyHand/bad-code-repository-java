package com.example.couriermanagement.warning.vehicle.impl;

import com.example.couriermanagement.entity.Vehicle;
import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.vehicle.EmptyVehiclesValidator;
import com.example.couriermanagement.warning.vehicle.data.EmptyVehiclesValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.couriermanagement.warning.ValidatorResponse.fail;
import static com.example.couriermanagement.warning.ValidatorResponse.ok;

@Component
@Order(6)
public class VehicleMaxVolumeValidator implements EmptyVehiclesValidator {

    private static final int MAX_VEHICLE_VOLUME = 50;

    @Override
    public ValidatorResponse validate(EmptyVehiclesValidatorContext context) {
        List<Vehicle> vehicles = context.getVehicles();
        Vehicle vehicle = vehicles.getFirst();
        int maxVolume = vehicle.getMaxVolume()
                .intValue();
        return maxVolume < MAX_VEHICLE_VOLUME
                ? fail("И объем маленький")
                : ok();
    }
}