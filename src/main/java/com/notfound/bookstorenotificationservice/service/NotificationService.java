package com.notfound.bookstorenotificationservice.service;

import com.notfound.bookstorenotificationservice.model.dto.CheckoutNotificationPayload;
import com.notfound.bookstorenotificationservice.model.dto.EmailVerificationEvent;
import com.notfound.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.notfound.bookstorenotificationservice.model.dto.OrderEventDto;
import com.notfound.bookstorenotificationservice.model.dto.PasswordResetOtpEvent;
import com.notfound.bookstorenotificationservice.model.dto.PaymentEventDto;

public interface NotificationService {
    void sendOrderNotification(OrderEventDto orderEvent);

    void sendPaymentNotification(PaymentEventDto paymentEvent);

    void sendPasswordResetOtpNotification(PasswordResetOtpEvent event);

    void sendEmailVerificationNotification(EmailVerificationEvent event);

    void sendEmailFallback(NotificationRequestDto request);

    /**
     * Xử lý event saga trên {@code bookstore.events}: checkout.completed, checkout.failed,
     * payment.completed, order.cancelled.
     */
    void sendSagaCheckoutNotification(String eventType, CheckoutNotificationPayload payload);
}
