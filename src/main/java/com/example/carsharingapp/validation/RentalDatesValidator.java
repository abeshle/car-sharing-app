package com.example.carsharingapp.validation;

import com.example.carsharingapp.model.Rental;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RentalDatesValidator implements ConstraintValidator<ValidRentalDates, Rental> {

    @Override
    public boolean isValid(Rental rental, ConstraintValidatorContext context) {
        if (rental.getRentalDate() == null || rental.getReturnDate() == null) {
            return true;
        }
        return !rental.getReturnDate().isBefore(rental.getRentalDate());
    }
}
