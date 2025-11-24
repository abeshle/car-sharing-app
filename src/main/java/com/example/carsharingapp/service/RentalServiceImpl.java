package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalResponseDto;
import com.example.carsharingapp.exceptions.EntityNotFoundException;
import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.car.CarRepository;
import com.example.carsharingapp.repository.payment.PaymentRepository;
import com.example.carsharingapp.repository.rental.RentalRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;

    @Transactional
    @Override
    public RentalResponseDto createRental(User user, CreateRentalRequestDto requestDto) {
        Car car = carRepository.findById(requestDto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found"));

        boolean hasPendingPayment = paymentRepository
                .existsByRental_User_IdAndStatus(user.getId(), PaymentStatus.PENDING);
        if (hasPendingPayment) {
            notificationService.notifyGeneralMessage(
                    "User " + user.getId() + " attempted to create rental but has pending payments."
            );
            throw new IllegalStateException(
                    "You cannot borrow a car until all pending payments are cleared");
        }
        if (car.getInventory() <= 0) {
            throw new IllegalStateException("Car is not available");
        }

        car.setInventory(car.getInventory() - 1);

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(requestDto.getReturnDate());

        rentalRepository.save(rental);
        carRepository.save(car);

        String summary = String.format("Car: %s, from %s to %s",
                rental.getCar().getModel(), rental.getRentalDate(), rental.getReturnDate());
        notificationService.notifyNewRental(rental.getId(), user.getId(), summary);

        return rentalMapper.toDto(rental);
    }

    @Override
    public Page<RentalResponseDto> getUserRentals(Long userId, Boolean active, Pageable pageable) {
        Page<Rental> rentals = rentalRepository.findAllByUserIdAndActive(userId, active, pageable);
        return rentals.map(rentalMapper::toDto);
    }

    @Override
    public RentalResponseDto getById(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

        if (!rental.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not your rental");
        }

        return rentalMapper.toDto(rental);
    }

    @Override
    public RentalResponseDto returnCar(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

        if (!rental.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not your rental");
        }

        if (!rental.isActive()) {
            throw new IllegalStateException("Rental already closed");
        }

        rental.setActive(false);
        rental.setActualReturnDate(LocalDate.now());

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);

        rentalRepository.save(rental);
        carRepository.save(car);

        return rentalMapper.toDto(rental);
    }

    @Override
    public Page<RentalResponseDto> getAllRentals(Boolean active, Long userId, Pageable pageable) {
        Page<Rental> rentals;

        if (userId != null) {
            rentals = rentalRepository.findAllByUserIdAndActive(userId, active, pageable);
        } else {
            rentals = rentalRepository.findAllByActive(active, pageable);
        }

        return rentals.map(rentalMapper::toDto);
    }
}
