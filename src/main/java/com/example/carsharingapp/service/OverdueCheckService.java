package com.example.carsharingapp.service;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.rental.RentalRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OverdueCheckService {

    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 5 0 * * *")
    public void checkOverdueRentals() {
        List<Rental> overdue = rentalRepository
                .findAllByActiveTrueAndReturnDateBefore(LocalDate.now());

        if (overdue.isEmpty()) {
            notificationService.notifyGeneralMessage("No rentals overdue today!");
            return;
        }

        for (Rental rental : overdue) {
            long daysLate = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());

            String info = """
                    Overdue Rental Warning!
                    User: %s %s (ID: %d)
                    Car: %s (ID: %d)
                    Should return: %s
                    Days late: %d
                    """.formatted(
                    rental.getUser().getFirstName(),
                    rental.getUser().getLastName(),
                    rental.getUser().getId(),
                    rental.getCar().getModel(),
                    rental.getCar().getId(),
                    rental.getReturnDate(),
                    daysLate
            );

            notificationService.notifyOverdueRental(rental.getId(), rental.getUser().getId(),info);
        }
    }
}
