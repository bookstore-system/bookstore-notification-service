package com.notfound.bookstorenotificationservice.service.impl;

import com.notfound.bookstorenotificationservice.client.UserContactResolver;
import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import com.notfound.bookstorenotificationservice.model.dto.PasswordResetOtpEvent;
import com.notfound.bookstorenotificationservice.service.MailDeliveryService;
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
}
