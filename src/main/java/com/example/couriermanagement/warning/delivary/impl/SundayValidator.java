package com.example.couriermanagement.warning.delivary.impl;

import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.couriermanagement.warning.ValidatorResponse.*;
import static java.time.DayOfWeek.SUNDAY;

@Component
@Order(1)
public class SundayValidator implements EmptyCouriersValidator {

    @Override
    public ValidatorResponse validate(EmptyCouriersValidatorContext context) {
        LocalDate date = context.getDate();
        return date != null && date.getDayOfWeek() == SUNDAY
                ? fail("Воскресенье - выходной день")
                : ok();
    }
}