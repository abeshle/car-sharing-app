package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.payment.CancelPaymentResponseDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto createPayment(Long rentalId);

    PaymentResponseDto confirmSuccess(String sessionId);

    CancelPaymentResponseDto cancelPayment(String sessionId);

    Page<PaymentResponseDto> getPayments(Long userId, Pageable pageable);

}
