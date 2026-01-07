package com.example.couriermanagement.warning;

public interface WarningValidator<T extends ValidationContext> {

    ValidatorResponse validate(T context);

}
