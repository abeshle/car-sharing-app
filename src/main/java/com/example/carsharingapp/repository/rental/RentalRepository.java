package com.example.carsharingapp.repository.rental;

import com.example.carsharingapp.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental,Long> {
    List<Rental> findAllByActiveTrueAndReturnDateBefore(LocalDate date);

    Page<Rental> findAllByUserIdAndActive(Long userId, Boolean active, Pageable pageable);

    Page<Rental> findAllByActive(Boolean active, Pageable pageable);
}
