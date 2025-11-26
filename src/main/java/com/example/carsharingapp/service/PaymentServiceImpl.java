package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.payment.CancelPaymentResponseDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.exceptions.EntityNotFoundException;
import com.example.carsharingapp.exceptions.PaymentProcessingException;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.PaymentType;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.payment.PaymentRepository;
import com.example.carsharingapp.repository.rental.RentalRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;
    private final StripeService stripeService;
    private final NotificationService notificationService;

    @Override
    public PaymentResponseDto createPayment(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Rental with id " + rentalId + " not found",
                                new Throwable("Cause: rental not present in the database")
                        )
                );

        BigDecimal amount = calculateAmount(rental);

        Session session;
        try {
            session = stripeService.createPaymentSession(amount, "Car Rental Payment");
        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to create Stripe payment session",e);
        }

        PaymentType type = rental.getReturnDate().isBefore(LocalDate.now())
                ? PaymentType.FINE
                : PaymentType.PAYMENT;

        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setAmount(amount);
        payment.setType(type);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDto confirmSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.PAID);
        notificationService
                .notifyPaymentSuccess(payment.getId(), payment.getRental().getUser().getId(),
                "Amount: " + payment.getAmount());
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public CancelPaymentResponseDto cancelPayment(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .map(this::buildPendingPaymentResponse)
                .orElse(buildPaymentNotFoundResponse());
    }

    @Override
    public Page<PaymentResponseDto> getPayments(Long userId, Pageable pageable) {
        return paymentRepository.findAllByRental_User_Id(userId, pageable)
                .map(paymentMapper::toDto);
    }

    private BigDecimal calculateAmount(Rental rental) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();

        long daysRented = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        if (daysRented <= 0) {
            daysRented = 1;
        }
        BigDecimal baseAmount = dailyFee.multiply(BigDecimal.valueOf(daysRented));

        if (LocalDate.now().isAfter(rental.getReturnDate())) {
            long daysLate = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());
            BigDecimal fine = dailyFee
                    .multiply(BigDecimal.valueOf(0.2))
                    .multiply(BigDecimal.valueOf(daysLate));
            return baseAmount.add(fine);
        }

        return baseAmount;
    }

    private CancelPaymentResponseDto buildPendingPaymentResponse(Payment payment) {
        CancelPaymentResponseDto response = new CancelPaymentResponseDto();
        response.setSuccess(true);
        response.setMessage("Payment remains pending");
        response.setPaymentId(payment.getId());
        response.setStatus(payment.getStatus());
        return response;
    }

    private CancelPaymentResponseDto buildPaymentNotFoundResponse() {
        CancelPaymentResponseDto response = new CancelPaymentResponseDto();
        response.setSuccess(false);
        response.setMessage("Payment not found");
        response.setPaymentId(null);
        response.setStatus(null);
        return response;
    }
}
