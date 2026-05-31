package com.notfound.bookstorenotificationservice.service.impl;

import com.notfound.bookstorenotificationservice.client.UserContactResolver;
import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import com.notfound.bookstorenotificationservice.model.dto.EmailVerificationEvent;
import com.notfound.bookstorenotificationservice.model.dto.OrderEventDto;
import com.notfound.bookstorenotificationservice.model.dto.PasswordResetOtpEvent;
import com.notfound.bookstorenotificationservice.service.MailDeliveryService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificationServiceImplTest {

    private static final String EMAIL_VERIFICATION_SUBJECT =
            "X\u00e1c th\u1ef1c email t\u00e0i kho\u1ea3n Nh\u00e0 S\u00e1ch C\u1ed9ng \u0110\u1ed3ng";

    @Test
    void sendOrderNotification_usesTypeAndOptionalOrderFieldsWhenStatusIsMissing() {
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        NotificationServiceImpl service = new NotificationServiceImpl(userContactResolver, mailDeliveryService);
        UUID orderId = UUID.randomUUID();
        OrderEventDto event = new OrderEventDto();
        event.setOrderId(orderId);
        event.setCustomerEmail("reader@example.com");
        event.setType("order.created");
        event.setTotalPrice(new BigDecimal("150000"));
        event.setPaymentMethod("COD");
        event.setCreatedAt(LocalDateTime.of(2026, 5, 31, 22, 0));
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

        service.sendOrderNotification(event);

        verify(mailDeliveryService).sendHtmlEmail(
                eq("reader@example.com"),
                eq("Cập nhật đơn hàng #" + orderId),
                htmlCaptor.capture(),
                eq((UUID) null),
                eq(orderId));
        assertThat(htmlCaptor.getValue())
                .contains("\u0110\u00e3 t\u1ea1o")
                .contains("150000 VND")
                .contains("COD")
                .contains("31/05/2026 22:00");
        verifyNoInteractions(userContactResolver);
    }

    @Test
    void sendPasswordResetOtpNotification_sendsOtpToEventEmailWithoutContactInfoLookup() {
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        NotificationServiceImpl service = new NotificationServiceImpl(userContactResolver, mailDeliveryService);
        PasswordResetOtpEvent event = new PasswordResetOtpEvent();
        event.setEventId(UUID.randomUUID());
        event.setEmail("reader@example.com");
        event.setDisplayName("Reader");
        event.setOtp("123456");
        event.setExpiresInMinutes(5);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

        service.sendPasswordResetOtpNotification(event);

        verify(mailDeliveryService).sendHtmlEmail(
                eq("reader@example.com"),
                eq("Ma OTP dat lai mat khau - NotFound Bookstore"),
                htmlCaptor.capture(),
                eq((UUID) null),
                eq((UUID) null));
        assertThat(htmlCaptor.getValue()).contains("123456");
        assertThat(htmlCaptor.getValue()).contains("5");
        verifyNoInteractions(userContactResolver);
    }

    @Test
    void sendPasswordResetOtpNotification_deliveryFailureDoesNotEscapeRabbitListener() {
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        NotificationServiceImpl service = new NotificationServiceImpl(userContactResolver, mailDeliveryService);
        PasswordResetOtpEvent event = new PasswordResetOtpEvent();
        event.setEventId(UUID.randomUUID());
        event.setEmail("reader@example.com");
        event.setOtp("654321");
        doThrow(new NotificationDeliveryException("smtp down"))
                .when(mailDeliveryService)
                .sendHtmlEmail(any(), any(), any(), any(), any());

        assertThatCode(() -> service.sendPasswordResetOtpNotification(event)).doesNotThrowAnyException();

        verify(mailDeliveryService).sendHtmlEmail(
                eq("reader@example.com"),
                eq("Ma OTP dat lai mat khau - NotFound Bookstore"),
                any(),
                eq((UUID) null),
                eq((UUID) null));
        verifyNoInteractions(userContactResolver);
    }

    @Test
    void sendEmailVerificationNotification_sendsVerificationLinkToEventEmailWithoutContactInfoLookup() {
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        NotificationServiceImpl service = new NotificationServiceImpl(userContactResolver, mailDeliveryService);
        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setEventId(UUID.randomUUID());
        event.setEmail("reader@example.com");
        event.setDisplayName("Reader");
        event.setVerificationUrl("http://localhost:8080/api/v1/auth/confirm-email?token=abc123");
        event.setExpiresInMinutes(1440);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

        service.sendEmailVerificationNotification(event);

        verify(mailDeliveryService).sendHtmlEmail(
                eq("reader@example.com"),
                eq(EMAIL_VERIFICATION_SUBJECT),
                htmlCaptor.capture(),
                eq((UUID) null),
                eq((UUID) null));
        assertThat(htmlCaptor.getValue())
                .contains("http://localhost:8080/api/v1/auth/confirm-email?token=abc123")
                .contains("1440")
                .contains("24");
        verifyNoInteractions(userContactResolver);
    }

    @Test
    void sendEmailVerificationNotification_deliveryFailureDoesNotEscapeRabbitListener() {
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        NotificationServiceImpl service = new NotificationServiceImpl(userContactResolver, mailDeliveryService);
        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setEventId(UUID.randomUUID());
        event.setEmail("reader@example.com");
        event.setVerificationUrl("http://localhost:8080/api/v1/auth/confirm-email?token=abc123");
        doThrow(new NotificationDeliveryException("smtp down"))
                .when(mailDeliveryService)
                .sendHtmlEmail(any(), any(), any(), any(), any());

        assertThatCode(() -> service.sendEmailVerificationNotification(event)).doesNotThrowAnyException();

        verify(mailDeliveryService).sendHtmlEmail(
                eq("reader@example.com"),
                eq(EMAIL_VERIFICATION_SUBJECT),
                any(),
                eq((UUID) null),
                eq((UUID) null));
        verifyNoInteractions(userContactResolver);
    }
}
