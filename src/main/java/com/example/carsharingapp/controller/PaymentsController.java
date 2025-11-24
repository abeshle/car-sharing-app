package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.payment.CancelPaymentResponseDto;
import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments management",
        description = "Endpoints for managing Stripe payments")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentsController {
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_MANAGER')")
    @GetMapping
    @Operation(summary = "Get all payments for user",
            description = "Retrieve all payments made by the authenticated user")
    public Page<PaymentResponseDto> getPayments(Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return paymentService.getPayments(user.getId(), pageable);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Stripe payment session",
            description = "Create a Stripe checkout session for a rental")
    public PaymentResponseDto createPayment(
            @RequestBody @Valid CreatePaymentRequestDto requestDto) {
        return paymentService.createPayment(requestDto.getRentalId());
    }

    @GetMapping("/success")
    @Operation(summary = "Stripe payment success",
            description = "Endpoint Stripe redirects to after successful payment")
    public PaymentResponseDto paymentSuccess(@RequestParam("session_id") String sessionId) {
        return paymentService.confirmSuccess(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Stripe payment canceled",
            description = "Endpoint Stripe redirects to if payment is canceled")
    public CancelPaymentResponseDto paymentCancel(@RequestParam("session_id") String sessionId) {
        return paymentService.cancelPayment(sessionId);
    }
}
