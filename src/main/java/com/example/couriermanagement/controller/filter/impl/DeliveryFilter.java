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

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryFilter implements Filter<Delivery> {

    @Parameter(description = "Фильтр по дате", example = "2025-01-30")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;

    @Parameter(description = "Фильтр по ID курьера", example = "1")
    Long courierId;

    @Parameter(description = "Фильтр по статусу")
    DeliveryStatus status;

    @Override
    public Specification<Delivery> filter() {
        return Specification.where(date())
                .and(courierId())
                .and(status());
    }

    private Specification<Delivery> date() {
        return (root, query, queryBuilder)
                -> date != null
                ? queryBuilder.equal(root.get("deliveryDate"), date)
                : null;
    }

    private Specification<Delivery> courierId() {
        return (root, query, queryBuilder)
                -> courierId != null
                ? queryBuilder.equal(root.get("courier").get("id"), courierId)
                : null;
    }

    private Specification<Delivery> status() {
        return (root, query, queryBuilder)
                -> status != null
                ? queryBuilder.equal(root.get("status"), status)
                : null;
    }
}
