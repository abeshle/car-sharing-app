package com.example.carsharingapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RentalDatesValidator.class)
@Documented
public @interface ValidRentalDates {
    String message() default "Return date cannot be before rental date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
