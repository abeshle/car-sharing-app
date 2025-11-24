package com.example.carsharingapp.service;

import com.example.carsharingapp.config.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeService {

    private final StripeConfig stripeConfig;

    public Session createPaymentSession(BigDecimal amount, String productName)
            throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeConfig.getSuccessUrl())
                .setCancelUrl(stripeConfig.getCancelUrl())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams
                                                .LineItem
                                                .PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(
                                                        amount.multiply(BigDecimal.valueOf(100))
                                                                .longValue())
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem
                                                                .PriceData.ProductData.builder()
                                                                .setName(productName)
                                                                .build())
                                                .build())
                                .build())
                .build();

        return Session.create(params);
    }

}
