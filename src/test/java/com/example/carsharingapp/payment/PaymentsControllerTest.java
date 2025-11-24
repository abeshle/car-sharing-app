package com.example.carsharingapp.payment;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingapp.dto.payment.CancelPaymentResponseDto;
import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.service.NotificationService;
import com.example.carsharingapp.service.PaymentService;
import com.example.carsharingapp.service.StripeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentsControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private NotificationService notificationService;

    @BeforeAll
    static void setup(@Autowired DataSource dataSource,
                      @Autowired WebApplicationContext context) throws SQLException {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                            new ClassPathResource("database/add-payments.sql"));
        }
    }

    @AfterAll
    static void cleanup(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/remove-all-payments.sql"));
        }
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }

        @Bean
        public PaymentMapper paymentMapper() {
            return Mockito.mock(PaymentMapper.class);
        }

        @Bean
        public StripeService stripeService() {
            return Mockito.mock(StripeService.class);
        }

        @Bean
        public NotificationService notificationService() {
            return Mockito.mock(NotificationService.class);
        }
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should create payment and return status 201")
    void createPayment_ShouldReturnCreated() throws Exception {
        CreatePaymentRequestDto req = new CreatePaymentRequestDto();
        req.setRentalId(1L);

        PaymentResponseDto response = new PaymentResponseDto();
        response.setRentalId(1L);

        Mockito.when(paymentService.createPayment(1L))
                .thenReturn(response);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails("customer@example.com")
    @DisplayName("Should return paginated list of payments for customer")
    void getPayments_ShouldReturnPage() throws Exception {
        PaymentResponseDto payment = new PaymentResponseDto();
        payment.setId(1L);
        payment.setRentalId(1L);

        Page<PaymentResponseDto> page = new PageImpl<>(List.of(payment));

        Mockito.when(paymentService.getPayments(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("Should process successful Stripe session and return PaymentResponseDto")
    void paymentSuccess_ShouldReturnPaymentResponse() throws Exception {

        PaymentResponseDto response = new PaymentResponseDto();
        response.setId(1L);
        response.setRentalId(1L);
        response.setSessionUrl("https://checkout.stripe.com/pay/test-session-id");

        String sessionId = "test-session-id";

        Mockito.when(paymentService.confirmSuccess(sessionId))
                .thenReturn(response);

        mockMvc.perform(get("/payments/success")
                        .param("session_id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rentalId").value(1))
                .andExpect(jsonPath("$.sessionUrl").value("https://checkout.stripe.com/pay/test-session-id"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("Should cancel Stripe session and return CancelPaymentResponseDto")
    void paymentCancel_ShouldReturnCancelPaymentResponse() throws Exception {

        CancelPaymentResponseDto cancelResponse = new CancelPaymentResponseDto();
        cancelResponse.setPaymentId(1L);
        cancelResponse.setSuccess(true);
        cancelResponse.setMessage("Payment remains pending");

        String sessionId = "test-session-id";

        Mockito.when(paymentService.cancelPayment(sessionId))
                .thenReturn(cancelResponse);

        mockMvc.perform(get("/payments/cancel")
                        .param("session_id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment remains pending"));
    }
}
