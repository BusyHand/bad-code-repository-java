package com.example.couriermanagement.controller.filter;

import org.springframework.data.jpa.domain.Specification;

public interface Filter<T> {

    Specification<T> filter();
}
