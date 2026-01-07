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
@Order(4)
public class IsMoreThenOneAvailableVehiclesValidator implements EmptyVehiclesValidator {

    @Override
    public ValidatorResponse validate(EmptyVehiclesValidatorContext context) {
        List<Vehicle> vehicles = context.getVehicles();
        return vehicles != null && !vehicles.isEmpty()
                ? fail("Хотя бы одна машина есть")
                : ok();
    }
}