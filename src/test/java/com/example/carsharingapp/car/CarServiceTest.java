package com.example.carsharingapp.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarResponseDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import com.example.carsharingapp.exceptions.EntityNotFoundException;
import com.example.carsharingapp.mapper.CarMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.repository.car.CarRepository;
import com.example.carsharingapp.service.CarServiceImpl;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Create car")
    public void create_shouldReturnCarResponseDto() {
        CarRequestDto requestDto = new CarRequestDto();
        requestDto.setModel("Tesla");

        Car car = new Car();
        car.setModel(requestDto.getModel());

        Car savedCar = new Car();
        savedCar.setId(2L);
        savedCar.setModel(car.getModel());

        CarResponseDto expectedCar = new CarResponseDto();
        expectedCar.setId(1L);
        expectedCar.setModel("Tesla");

        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(savedCar);
        when(carMapper.toDto(savedCar)).thenReturn(expectedCar);

        CarResponseDto result = carService.create(requestDto);

        assertEquals(expectedCar, result);

        verify(carMapper).toModel(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(savedCar);
    }

    @Test
    @DisplayName("Find all cars")
    public void findAll_shouldReturnAllCars() {
        Car car = new Car();
        car.setId(1L);
        car.setModel("Tesla");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(Collections.singletonList(car));

        CarResponseDto expected = new CarResponseDto();
        expected.setId(1L);
        expected.setModel("Tesla");

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(expected);

        Page<CarResponseDto> result = carService.findAll(pageable);
        CarResponseDto actual = result.getContent().get(0);

        assertEquals(expected, actual);

        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Get car by valid ID")
    public void getById_withValidId_shouldReturnCar() {
        Long id = 1L;
        Car car = new Car();
        car.setId(id);
        car.setModel("Tesla");

        CarResponseDto expectedDto = new CarResponseDto();
        expectedDto.setId(id);
        expectedDto.setModel("Tesla");

        when(carRepository.findById(id)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedDto);

        CarResponseDto result = carService.getById(id);

        assertEquals(expectedDto, result);

        verify(carRepository).findById(id);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Get car by non-existing ID should throw exception")
    public void getById_withNonExistingId_shouldThrowException() {
        Long id = 10L;

        when(carRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.getById(id)
        );

        String expectedMessage = "Entity with id " + id + " not found";
        assertEquals(expectedMessage, exception.getMessage());

        verify(carRepository).findById(id);
    }

    @Test
    @DisplayName("Update car with valid ID")
    public void update_withValidId_shouldReturnUpdatedCar() {
        Long id = 1L;

        Car existingCar = new Car();
        existingCar.setId(id);
        existingCar.setModel("Old Model");

        UpdateCarRequestDto updateDto = new UpdateCarRequestDto();
        updateDto.setModel("Updated Model");

        CarResponseDto expectedDto = new CarResponseDto();
        expectedDto.setId(id);
        expectedDto.setModel("Updated Model");

        when(carRepository.findById(id)).thenReturn(Optional.of(existingCar));
        Mockito.doAnswer(invocation -> {
            UpdateCarRequestDto dto = invocation.getArgument(0);
            Car carToUpdate = invocation.getArgument(1);
            carToUpdate.setModel(dto.getModel());
            return null;
        }).when(carMapper).updateCarFromDto(updateDto, existingCar);

        when(carRepository.save(existingCar)).thenReturn(existingCar);
        when(carMapper.toDto(existingCar)).thenReturn(expectedDto);

        CarResponseDto result = carService.update(id, updateDto);

        assertEquals(expectedDto, result);

        verify(carRepository).findById(id);
        verify(carMapper).updateCarFromDto(updateDto, existingCar);
        verify(carRepository).save(existingCar);
        verify(carMapper).toDto(existingCar);
    }

    @Test
    @DisplayName("Delete car")
    public void delete_shouldDeleteCar() {
        Long id = 1L;

        Mockito.doNothing().when(carRepository).deleteById(id);

        carService.delete(id);

        verify(carRepository).deleteById(id);
    }
}
