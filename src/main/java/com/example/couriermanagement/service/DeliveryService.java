package com.example.couriermanagement.service;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.controller.filter.impl.DeliveryFilter;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.dto.request.GenerateDeliveriesRequest;
import com.example.couriermanagement.dto.response.GenerateDeliveriesResponse;
import com.example.couriermanagement.entity.Delivery;

import java.util.List;

public interface DeliveryService {
    List<DeliveryDto> getAll(Filter<Delivery> deliveryFilter);

    DeliveryDto getDeliveryById(Long id);

    DeliveryDto createDelivery(DeliveryRequest deliveryRequest);

    DeliveryDto updateDelivery(Long id, DeliveryRequest deliveryRequest);

    void deleteDelivery(Long id);

    GenerateDeliveriesResponse generateDeliveries(GenerateDeliveriesRequest generateRequest);
}