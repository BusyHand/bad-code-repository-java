package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.service.OpenStreetMapService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class OpenStreetMapServiceImpl implements OpenStreetMapService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String openRouteServiceUrl = "https://api.openrouteservice.org/v2/directions/driving-car";

    public OpenStreetMapServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public BigDecimal calculateDistance(
            BigDecimal startLatitude,
            BigDecimal startLongitude,
            BigDecimal endLatitude,
            BigDecimal endLongitude) {
        try {
            String url = String.format("%s?start=%s,%s&end=%s,%s",
                    openRouteServiceUrl, startLongitude, startLatitude, endLongitude, endLatitude);

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                throw new RuntimeException("Failed to get response from OpenStreetMap");
            }

            JsonNode jsonNode = objectMapper.readTree(response);
            BigDecimal distance = extractDistanceFromResponse(jsonNode);

            return distance.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            return calculateHaversineDistance(startLatitude, startLongitude, endLatitude, endLongitude);
        }
    }

    private BigDecimal extractDistanceFromResponse(JsonNode jsonNode) {
        JsonNode features = jsonNode.get("features");
        if (features != null && features.isArray() && features.size() > 0) {
            JsonNode properties = features.get(0).get("properties");
            if (properties != null) {
                JsonNode summary = properties.get("summary");
                if (summary != null) {
                    JsonNode distance = summary.get("distance");
                    if (distance != null) {
                        return BigDecimal.valueOf(distance.asDouble());
                    }
                }
            }
        }
        throw new RuntimeException("Unable to extract distance from OpenStreetMap response");
    }

    private BigDecimal calculateHaversineDistance(
            BigDecimal lat1,
            BigDecimal lon1,
            BigDecimal lat2,
            BigDecimal lon2) {
        double earthRadius = 6371.0;

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double sinDLatHalf = Math.sin(dLat / 2);
        double sinDLonHalf = Math.sin(dLon / 2);

        double a = sinDLatHalf * sinDLatHalf +
                Math.cos(Math.toRadians(lat1.doubleValue())) *
                        Math.cos(Math.toRadians(lat2.doubleValue())) *
                        sinDLonHalf * sinDLonHalf;

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BigDecimal.valueOf(earthRadius * c).setScale(2, RoundingMode.HALF_UP);
    }
}