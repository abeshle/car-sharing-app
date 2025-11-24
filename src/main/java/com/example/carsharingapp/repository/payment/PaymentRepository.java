package com.example.carsharingapp.repository.payment;

import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    boolean existsByRental_User_IdAndStatus(Long userId, PaymentStatus status);

    Optional<Payment> findBySessionId(String sessionId);

    Page<Payment> findAllByRental_User_Id(Long userId, Pageable pageable);

    List<Payment> findAllByStatus(PaymentStatus paymentStatus);
}
