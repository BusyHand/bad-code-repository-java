package com.example.couriermanagement.controller.filter.impl;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.entity.UserRole;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;

import static org.springframework.data.jpa.domain.Specification.where;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserFilter implements Filter<User> {

    @Parameter(description = "Фильтр по роли пользователя")
    UserRole role;

    @Override
    public Specification<User> filter() {
        return where(role());
    }

    private Specification<User> role() {
        return (root, query, queryBuilder)
                -> role != null
                ? queryBuilder.equal(root.get("role"), role)
                : null;
    }

}
