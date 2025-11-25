package com.example.carsharingapp.service;

import com.example.carsharingapp.config.TelegramConfig;
import com.example.carsharingapp.exceptions.NotificationSendException;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    private void sendMessage(String text) {
        String token = telegramConfig.getBotToken();
        String chatId = telegramConfig.getChatId();
        URI uri = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host("api.telegram.org")
                .path("/bot{token}/sendMessage")
                .buildAndExpand(Map.of("token", token))
                .toUri();

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "parse_mode", "Markdown"
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, body, String.class);
        } catch (Exception ex) {
            throw new NotificationSendException("Failed to send telegram notification",ex);
        }
    }

    @Override
    @Async("notificationExecutor")
    public void notifyNewRental(Long rentalId, Long userId, String summary) {
        String text =
                String.format("📣 *New rental created*\nRental id: %d\nUser id: %d\n%s",
                        rentalId, userId, summary);
        sendMessage(text);
    }

    @Override
    @Async("notificationExecutor")
    public void notifyOverdueRental(Long rentalId, Long userId, String info) {
        String text =
                String.format("⚠️ *Overdue rental*\nRental id: %d\nUser id: %d\nDetailed info %s",
                        rentalId, userId, info);
        sendMessage(text);
    }

    @Override
    @Async("notificationExecutor")
    public void notifyPaymentSuccess(Long paymentId, Long userId, String summary) {
        String text =
                String.format("✅ *Payment succeeded*\nPayment id: %d\nUser id: %d\n%s",
                        paymentId, userId, summary);
        sendMessage(text);
    }

    @Override
    @Async("notificationExecutor")
    public void notifyGeneralMessage(String message) {
        sendMessage("ℹ️ " + message);
    }
}
