package com.example.couriermanagement.service;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.response.CourierDeliveryResponse;
import com.example.couriermanagement.entity.Delivery;

import java.util.List;

public interface CourierService {
    List<CourierDeliveryResponse> getCourierDeliveries(Filter<Delivery> filter);

    DeliveryDto getCourierDeliveryById(Long id);
}