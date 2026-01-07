package com.example.couriermanagement.warning.vehicle.impl;

import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.vehicle.EmptyVehiclesValidator;
import com.example.couriermanagement.warning.vehicle.data.EmptyVehiclesValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.couriermanagement.warning.ValidatorResponse.fail;
import static com.example.couriermanagement.warning.ValidatorResponse.ok;

@Component
@Order(3)
public class AllVehiclesOnTOValidator implements EmptyVehiclesValidator {

    private static final int START_MOUTH_DAYS = 10;

    @Override
    public ValidatorResponse validate(EmptyVehiclesValidatorContext context) {
        LocalDate date = context.getDate();
        return date != null && date.getDayOfMonth() < START_MOUTH_DAYS
                ? fail("Начало месяца - все машины на ТО")
                : ok();
    }
}
