package com.example.couriermanagement.warning.delivary.impl;

import com.example.couriermanagement.entity.Vehicle;
import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.couriermanagement.warning.ValidatorResponse.*;

@Component
@Order(5)
public class AvailableVehiclesValidator implements EmptyCouriersValidator {

    @Override
    public ValidatorResponse validate(EmptyCouriersValidatorContext context) {
        List<Vehicle> vehicles = context.getVehicles();
        return vehicles != null && vehicles.isEmpty()
                ? fail("Машины тоже заняты")
                : ok();
    }
}