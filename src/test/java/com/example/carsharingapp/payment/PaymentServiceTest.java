package com.example.carsharingapp.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.dto.payment.CancelPaymentResponseDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.payment.PaymentRepository;
import com.example.carsharingapp.repository.rental.RentalRepository;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.PaymentServiceImpl;
import com.example.carsharingapp.service.StripeService;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private StripeService stripeService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Rental rental;

    @BeforeEach
    void setUp() {
        rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(LocalDate.now().minusDays(2));
        rental.setReturnDate(LocalDate.now().plusDays(3));

        Car car = new Car();
        car.setDailyFee(BigDecimal.valueOf(100));
        rental.setCar(car);

        User user = new User();
        user.setId(10L);
        rental.setUser(user);
    }

    @Test
    @DisplayName("Create payment should return correct DTO")
    void createPayment_shouldReturnPaymentResponseDto() throws Exception {
        Payment payment = new Payment();
        Session session = new Session();
        session.setId("sess_123");
        session.setUrl("http://payment.url");

        PaymentResponseDto expectedDto = new PaymentResponseDto();
        expectedDto.setId(1L);

        when(rentalRepository.findById(1L))
                .thenReturn(Optional.of(rental));
        when(stripeService.createPaymentSession(any(BigDecimal.class),
                anyString())).thenReturn(session);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(expectedDto);

        PaymentResponseDto result = paymentService.createPayment(1L);

        assertNotNull(result);
        assertEquals(expectedDto, result);

        verify(rentalRepository).findById(1L);
        verify(stripeService).createPaymentSession(any(BigDecimal.class), anyString());
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDto(any(Payment.class));
    }

    @Test
    @DisplayName("Confirm payment success should update status and notify user")
    void confirmSuccess_shouldUpdateStatusAndNotify() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setRental(rental);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setStatus(PaymentStatus.PENDING);

        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(1L);

        when(paymentRepository.findBySessionId("sess_123"))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(dto);
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponseDto result = paymentService.confirmSuccess("sess_123");

        assertNotNull(result);
        assertEquals(dto,result);
        verify(notificationService).notifyPaymentSuccess(payment.getId(),
                rental.getUser().getId(), "Amount: " + payment.getAmount());
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toDto(payment);
    }

    @Test
    @DisplayName("Cancel payment returns pending response if exists")
    void cancelPayment_pending() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findBySessionId("sess_123")).thenReturn(Optional.of(payment));

        CancelPaymentResponseDto result = paymentService.cancelPayment("sess_123");

        assertTrue(result.isSuccess());
        assertEquals(payment.getId(), result.getPaymentId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("Cancel payment returns not found response if payment missing")
    void cancelPayment_notFound() {
        when(paymentRepository.findBySessionId("sess_123")).thenReturn(Optional.empty());

        CancelPaymentResponseDto result = paymentService.cancelPayment("sess_123");

        assertFalse(result.isSuccess());
        assertNull(result.getPaymentId());
        assertNull(result.getStatus());
    }

    @Test
    @DisplayName("Get payments returns paginated DTOs")
    void getPayments_returnsPaginatedDtos() {
        Payment payment = new Payment();
        payment.setId(1L);

        Page<Payment> page = new PageImpl<>(List.of(payment));
        when(paymentRepository.findAllByRental_User_Id(10L, Pageable.unpaged())).thenReturn(page);

        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(1L);
        when(paymentMapper.toDto(payment)).thenReturn(dto);

        Page<PaymentResponseDto> result = paymentService.getPayments(10L, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(dto.getId(), result.getContent().get(0).getId());
    }
}
