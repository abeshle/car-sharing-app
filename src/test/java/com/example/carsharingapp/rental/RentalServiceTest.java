package com.example.carsharingapp.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalResponseDto;
import com.example.carsharingapp.exceptions.EntityNotFoundException;
import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.car.CarRepository;
import com.example.carsharingapp.repository.payment.PaymentRepository;
import com.example.carsharingapp.repository.rental.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.RentalServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Should create rental successfully")
    void createRental_shouldReturnRentalResponseDto() {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto();
        requestDto.setCarId(1L);
        requestDto.setReturnDate(LocalDate.now().plusDays(3));

        User user = new User();
        user.setId(1L);

        Car car = new Car();
        car.setId(1L);
        car.setInventory(2);
        car.setModel("Tesla");

        Rental savedRental = new Rental();
        savedRental.setId(10L);
        savedRental.setCar(car);
        savedRental.setUser(user);
        savedRental.setRentalDate(LocalDate.now());
        savedRental.setReturnDate(requestDto.getReturnDate());
        savedRental.setActive(true);

        RentalResponseDto expectedDto = new RentalResponseDto();
        expectedDto.setId(10L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(rentalRepository.save(any(Rental.class))).thenReturn(savedRental);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(expectedDto);

        doNothing().when(notificationService)
                .notifyNewRental(any(), anyLong(), anyString());

        RentalResponseDto result = rentalService.createRental(user, requestDto);

        assertEquals(expectedDto.getId(), result.getId());
        verify(carRepository).findById(1L);
        verify(rentalRepository).save(any(Rental.class));
        verify(carRepository).save(car);
        verify(rentalMapper).toDto(any(Rental.class));
    }

    @Test
    @DisplayName("Should throw when car not found")
    void createRental_shouldThrowIfCarNotFound() {
        CreateRentalRequestDto request = new CreateRentalRequestDto();
        request.setCarId(999L);

        User user = new User();
        user.setId(1L);

        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.createRental(user, request));

        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when car unavailable")
    void createRental_shouldThrowIfCarUnavailable() {
        Car car = new Car();
        car.setId(1L);
        car.setInventory(0);

        User user = new User();
        user.setId(1L);

        CreateRentalRequestDto request = new CreateRentalRequestDto();
        request.setCarId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(paymentRepository.existsByRental_User_IdAndStatus(anyLong(), any()))
                .thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> rentalService.createRental(user, request));
    }

    @Test
    @DisplayName("Should return user's rentals")
    void getUserRentals_shouldReturnPage() {
        User user = new User();
        user.setId(1L);

        Car car = new Car();
        car.setId(1L);

        Rental rental = new Rental();
        rental.setId(5L);
        rental.setUser(user);
        rental.setCar(car);
        rental.setActive(true);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Rental> rentalsPage = new PageImpl<>(List.of(rental));

        RentalResponseDto dto = new RentalResponseDto();
        dto.setId(5L);

        when(rentalRepository.findAllByUserIdAndActive(1L, true, pageable))
                .thenReturn(rentalsPage);
        when(rentalMapper.toDto(rental)).thenReturn(dto);

        Page<RentalResponseDto> result = rentalService.getUserRentals(1L, true, pageable);

        assertEquals(1, result.getTotalElements());
        verify(rentalRepository).findAllByUserIdAndActive(1L, true, pageable);
    }

    @Test
    @DisplayName("Should return rental by ID")
    void getById_shouldReturnRental() {
        User user = new User();
        user.setId(1L);

        Car car = new Car();
        car.setId(1L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(user);
        rental.setCar(car);

        RentalResponseDto expectedDto = new RentalResponseDto();
        expectedDto.setId(1L);
        expectedDto.setCarId(1L);
        expectedDto.setUserId(1L);
        expectedDto.setRentalDate(rental.getRentalDate());
        expectedDto.setReturnDate(rental.getReturnDate());
        expectedDto.setActive(true);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        RentalResponseDto actualDto = rentalService.getById(1L, 1L);

        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);
        verify(rentalRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw when rental not yours")
    void getById_shouldThrowIfNotYourRental() {
        User owner = new User();
        owner.setId(1L);

        User anotherUser = new User();
        anotherUser.setId(2L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(owner);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        assertThrows(AccessDeniedException.class,
                () -> rentalService.getById(1L, anotherUser.getId()));
    }

    @Test
    @DisplayName("Should close rental when returning car")
    void returnCar_shouldCloseRental() {
        User user = new User();
        user.setId(1L);

        Car car = new Car();
        car.setId(1L);
        car.setInventory(1);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(user);
        rental.setCar(car);
        rental.setActive(true);

        RentalResponseDto dto = new RentalResponseDto();
        dto.setId(1L);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(carRepository.save(car)).thenReturn(car);
        when(rentalMapper.toDto(rental)).thenReturn(dto);

        RentalResponseDto result = rentalService.returnCar(1L, 1L);

        assertNotNull(result);
        assertFalse(rental.isActive());
        verify(carRepository).save(car);
        verify(rentalRepository).save(rental);
    }

    @Test
    @DisplayName("Should throw if returning rental not yours")
    void returnCar_shouldThrowIfNotYourRental() {
        User owner = new User();
        owner.setId(1L);

        User anotherUser = new User();
        anotherUser.setId(2L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(owner);
        rental.setActive(true);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        assertThrows(AccessDeniedException.class,
                () -> rentalService.returnCar(1L, anotherUser.getId()));
    }
}
