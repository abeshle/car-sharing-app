package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarResponseDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import com.example.carsharingapp.exceptions.EntityNotFoundException;
import com.example.carsharingapp.mapper.CarMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.repository.car.CarRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CarServiceImpl implements CarService {
    private final CarMapper carMapper;

    private final CarRepository carRepository;

    @Override
    public CarResponseDto create(CarRequestDto requestDto) {
        return carMapper.toDto(carRepository.save(carMapper.toModel(requestDto)));
    }

    @Override
    public Page<CarResponseDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    public CarResponseDto getById(Long id) {
        return carRepository.findById(id)
                .map(carMapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entity with id " + id + " not found"));
    }

    @Override
    public CarResponseDto update(Long id, UpdateCarRequestDto requestDto) {
        Car car = carRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entity with id " + id + " not found"));

        carMapper.updateCarFromDto(requestDto, car);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public void delete(Long id) {
        carRepository.deleteById(id);
    }
}
