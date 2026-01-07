package com.example.couriermanagement.warning.vehicle.impl;

import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.vehicle.EmptyVehiclesValidator;
import com.example.couriermanagement.warning.vehicle.data.EmptyVehiclesValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.couriermanagement.warning.ValidatorResponse.fail;
import static com.example.couriermanagement.warning.ValidatorResponse.ok;
import static java.time.DayOfWeek.SATURDAY;

@Component
@Order(1)
public class SaturdayValidator implements EmptyVehiclesValidator {

    @Override
    public ValidatorResponse validate(EmptyVehiclesValidatorContext context) {
        LocalDate date = context.getDate();
        return date != null && date.getDayOfWeek() == SATURDAY
                ? fail("Суббота - мало машин")
                : ok();
    }
}
