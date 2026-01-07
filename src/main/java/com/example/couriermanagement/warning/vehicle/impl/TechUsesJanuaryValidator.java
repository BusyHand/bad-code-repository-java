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
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;

@Component
@Order(2)
public class TechUsesJanuaryValidator implements EmptyVehiclesValidator {

    @Override
    public ValidatorResponse validate(EmptyVehiclesValidatorContext context) {
        LocalDate date = context.getDate();
        return date != null && date.getMonth() == JANUARY
                ? fail("Январь - техническое обслуживание")
                : ok();
    }
}
