package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.response.CourierDeliveryResponse;
import com.example.couriermanagement.entity.DeliveryStatus;

import java.time.LocalDate;
import java.util.List;

public interface CourierService {
    List<CourierDeliveryResponse> getCourierDeliveries(
            LocalDate date,
            DeliveryStatus status,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    DeliveryDto getCourierDeliveryById(Long id);
}