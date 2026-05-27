package com.notfound.bookstorenotificationservice.messaging.consumer;

import com.notfound.bookstorenotificationservice.model.dto.OrderEventDto;
import com.notfound.bookstorenotificationservice.model.dto.EmailVerificationEvent;
import com.notfound.bookstorenotificationservice.model.dto.PasswordResetOtpEvent;
import com.notfound.bookstorenotificationservice.model.dto.PaymentEventDto;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationConsumerTest {

    @Test
    void handleOrderEvent_delegatesToService() {
        NotificationService service = mock(NotificationService.class);
        NotificationConsumer consumer = new NotificationConsumer(service);

        OrderEventDto order = new OrderEventDto();
        order.setOrderId(UUID.randomUUID());
        order.setCustomerEmail("a@b.com");
        order.setCustomerName("A");
        order.setStatus("CREATED");

        consumer.handleOrderEvent(order);

        verify(service).sendOrderNotification(order);
    }

    @Test
    void handlePaymentEvent_delegatesToService() {
        NotificationService service = mock(NotificationService.class);
        NotificationConsumer consumer = new NotificationConsumer(service);

        PaymentEventDto payment = new PaymentEventDto();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(UUID.randomUUID());
        payment.setCustomerEmail("pay@b.com");
        payment.setStatus("SUCCESS");

        consumer.handlePaymentEvent(payment);

        verify(service).sendPaymentNotification(payment);
    }

    @Test
    void handlePasswordResetEvent_delegatesToService() {
        NotificationService service = mock(NotificationService.class);
        NotificationConsumer consumer = new NotificationConsumer(service);

        PasswordResetOtpEvent dto = new PasswordResetOtpEvent();
        dto.setEmail("u@b.com");
        dto.setOtp("123456");

        consumer.handlePasswordResetEvent(dto);

        verify(service).sendPasswordResetOtpNotification(dto);
    }

    @Test
    void handleEmailVerificationEvent_delegatesToService() {
        NotificationService service = mock(NotificationService.class);
        NotificationConsumer consumer = new NotificationConsumer(service);

        EmailVerificationEvent dto = new EmailVerificationEvent();
        dto.setEmail("u@b.com");
        dto.setVerificationUrl("http://localhost:8080/api/v1/auth/confirm-email?token=abc");

        consumer.handleEmailVerificationEvent(dto);

        verify(service).sendEmailVerificationNotification(dto);
    }
}
