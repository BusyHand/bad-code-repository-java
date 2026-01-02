package com.example.couriermanagement.controller.filter.impl;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.data.jpa.domain.Specification.where;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourierDeliveryFilter implements Filter<Delivery> {

    @Parameter(description = "Фильтр по дате доставки", example = "2025-01-30")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;

    @Parameter(description = "Фильтр по статусу")
    DeliveryStatus status;

    @Parameter(description = "Начальная дата периода", example = "2025-01-25")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateFrom;

    @Parameter(description = "Конечная дата периода", example = "2025-01-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateTo;

    @Override
    public Specification<Delivery> filter() {
        return where(date())
                .and(status())
                .and(dateFromAndDateTo());
    }

    private Specification<Delivery> date() {
        return (root, query, queryBuilder)
                -> date != null
                ? queryBuilder.equal(root.get("deliveryDate"), date)
                : null;
    }

    private Specification<Delivery> status() {
        return (root, query, queryBuilder)
                -> status != null
                ? queryBuilder.equal(root.get("status"), status)
                : null;
    }

    private Specification<Delivery> dateFromAndDateTo() {
        return (root, query, queryBuilder)
                -> dateFrom != null && dateTo != null
                ? queryBuilder.between(root.get("deliveryDate"), dateFrom, dateTo)
                : null;
    }
}
