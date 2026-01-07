package com.example.couriermanagement.warning.delivary.impl;

import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.couriermanagement.warning.ValidatorResponse.*;

@Component
@Order(3)
public class NewYearHolidayValidator implements EmptyCouriersValidator {

    private static final int NEW_YEARS_HOLIDAYS_START_DAYS = 25;

    @Override
    public ValidatorResponse validate(EmptyCouriersValidatorContext context) {
        LocalDate date = context.getDate();
        return date != null && date.getDayOfMonth() > NEW_YEARS_HOLIDAYS_START_DAYS
                ? fail("Новогодние праздники")
                : ok();
    }
}