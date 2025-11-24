package com.example.carsharingapp.service;

public interface NotificationService {
    void notifyNewRental(Long rentalId, Long userId, String summary);

    void notifyOverdueRental(Long rentalId, Long userId, String info);

    void notifyPaymentSuccess(Long paymentId, Long userId, String summary);

    void notifyGeneralMessage(String message);
}
