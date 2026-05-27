package com.notfound.bookstorenotificationservice.service.impl;

import com.notfound.bookstorenotificationservice.client.UserContactInfoResponse;
import com.notfound.bookstorenotificationservice.client.UserContactResolver;
import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import com.notfound.bookstorenotificationservice.messaging.SagaEventTypes;
import com.notfound.bookstorenotificationservice.model.dto.CheckoutNotificationPayload;
import com.notfound.bookstorenotificationservice.model.dto.EmailVerificationEvent;
import com.notfound.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.notfound.bookstorenotificationservice.model.dto.OrderEventDto;
import com.notfound.bookstorenotificationservice.model.dto.PasswordResetOtpEvent;
import com.notfound.bookstorenotificationservice.model.dto.PaymentEventDto;
import com.notfound.bookstorenotificationservice.service.MailDeliveryService;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import com.notfound.bookstorenotificationservice.util.BookstoreNotificationHtmlBuilder;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String EMAIL_VERIFICATION_SUBJECT =
            "X\u00e1c th\u1ef1c email t\u00e0i kho\u1ea3n Nh\u00e0 S\u00e1ch C\u1ed9ng \u0110\u1ed3ng";
    private final UserContactResolver userContactResolver;
    private final MailDeliveryService mailDeliveryService;

    public NotificationServiceImpl(
            UserContactResolver userContactResolver,
            MailDeliveryService mailDeliveryService) {
        this.userContactResolver = userContactResolver;
        this.mailDeliveryService = mailDeliveryService;
    }

    @Override
    public void sendOrderNotification(OrderEventDto orderEvent) {
        logger.info("Processing Order Notification for ID: {}", orderEvent.getOrderId());
        String email = resolveRecipientEmail(orderEvent.getCustomerEmail(), orderEvent.getUserId());
        String customerName = orderEvent.getCustomerName();

        String totalStr = orderEvent.getTotalPrice() != null ? orderEvent.getTotalPrice().toPlainString() : null;
        String orderIdStr = orderEvent.getOrderId() != null ? orderEvent.getOrderId().toString() : null;
        String inner = BookstoreNotificationHtmlBuilder.buildOrderNotificationBody(
                customerName, orderIdStr, orderEvent.getStatus(), totalStr);
        String subject = "Cập nhật đơn hàng" + (orderIdStr != null ? " #" + orderIdStr : "");
        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(subject, inner);

        logger.info("Order notification for {} (HTML {} chars). Plain: Hello {}, order {} is {}.",
                email,
                html.length(),
                customerName,
                orderEvent.getOrderId(),
                orderEvent.getStatus());
        mailDeliveryService.sendHtmlEmail(email, subject, html, null, orderEvent.getOrderId());
    }

    @Override
    public void sendPaymentNotification(PaymentEventDto paymentEvent) {
        logger.info("Processing Payment Notification for paymentId={}, orderId={}",
                paymentEvent.getPaymentId(), paymentEvent.getOrderId());
        String email = resolveRecipientEmail(paymentEvent.getCustomerEmail(), paymentEvent.getUserId());
        String customerName = paymentEvent.getCustomerName();

        String amountStr = paymentEvent.getAmount() != null ? paymentEvent.getAmount().toPlainString() : null;
        String paymentIdStr = paymentEvent.getPaymentId() != null ? paymentEvent.getPaymentId().toString() : null;
        String orderIdStr = paymentEvent.getOrderId() != null ? paymentEvent.getOrderId().toString() : null;
        String currency = paymentEvent.getCurrency() != null && !paymentEvent.getCurrency().isBlank()
                ? paymentEvent.getCurrency()
                : "VND";

        String inner = BookstoreNotificationHtmlBuilder.buildPaymentNotificationBody(
                customerName,
                paymentIdStr,
                orderIdStr,
                amountStr,
                currency,
                paymentEvent.getStatus(),
                paymentEvent.getPaymentMethod());
        String subject = "Thông báo thanh toán"
                + (paymentIdStr != null ? " — " + paymentIdStr : "");
        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(subject, inner);

        logger.info("Payment notification for {} (HTML {} chars). status={}", email, html.length(), paymentEvent.getStatus());

        mailDeliveryService.sendHtmlEmail(email, subject, html, null, paymentEvent.getOrderId());
    }

    @Override
    public void sendPasswordResetOtpNotification(PasswordResetOtpEvent event) {
        if (event == null) {
            logger.warn("Skip password-reset OTP email: event is null.");
            return;
        }
        logger.info(
                "Processing password-reset OTP notification. eventId={}, userId={}",
                event.getEventId(),
                event.getUserId());
        if (!StringUtils.hasText(event.getEmail())) {
            logger.warn("Skip password-reset OTP email: missing email. eventId={}", event.getEventId());
            return;
        }

        if (!StringUtils.hasText(event.getOtp())) {
            logger.warn("Skip password-reset OTP email: missing otp. eventId={}", event.getEventId());
            return;
        }

        String email = event.getEmail().trim();
        int expires = event.getExpiresInMinutes() != null && event.getExpiresInMinutes() > 0
                ? event.getExpiresInMinutes()
                : 5;
        String inner = BookstoreNotificationHtmlBuilder.buildPasswordResetOtpBody(
                event.getDisplayName(), event.getOtp().trim(), expires);
        String subject = "Ma OTP dat lai mat khau - NotFound Bookstore";
        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(subject, inner);

        logger.info("Password-reset OTP email prepared for {} (HTML {} chars). expiresInMinutes={}",
                email, html.length(), expires);
        try {
            mailDeliveryService.sendHtmlEmail(email, subject, html, null, null);
        } catch (NotificationDeliveryException e) {
            logger.warn(
                    "Skip password-reset OTP email after delivery failure. eventId={}, email={}, reason={}",
                    event.getEventId(),
                    email,
                    e.getMessage());
        } catch (RuntimeException e) {
            logger.warn(
                    "Skip password-reset OTP email after unexpected delivery failure. eventId={}, email={}",
                    event.getEventId(),
                    email,
                    e);
        }
    }

    @Override
    public void sendEmailVerificationNotification(EmailVerificationEvent event) {
        if (event == null) {
            logger.warn("Skip email verification email: event is null.");
            return;
        }
        logger.info(
                "Processing email verification notification. eventId={}, userId={}",
                event.getEventId(),
                event.getUserId());
        if (!StringUtils.hasText(event.getEmail())) {
            logger.warn("Skip email verification email: missing email. eventId={}", event.getEventId());
            return;
        }

        if (!StringUtils.hasText(event.getVerificationUrl())) {
            logger.warn("Skip email verification email: missing verificationUrl. eventId={}", event.getEventId());
            return;
        }

        String email = event.getEmail().trim();
        String verificationUrl = event.getVerificationUrl().trim();
        int expires = event.getExpiresInMinutes() != null && event.getExpiresInMinutes() > 0
                ? event.getExpiresInMinutes()
                : 1440;
        String inner = BookstoreNotificationHtmlBuilder.buildEmailVerificationBody(
                event.getDisplayName(), verificationUrl, expires);
        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(EMAIL_VERIFICATION_SUBJECT, inner);

        logger.info(
                "Email verification email prepared for {} (HTML {} chars). expiresInMinutes={}",
                email,
                html.length(),
                expires);
        try {
            mailDeliveryService.sendHtmlEmail(email, EMAIL_VERIFICATION_SUBJECT, html, null, null);
        } catch (NotificationDeliveryException e) {
            logger.warn(
                    "Skip email verification email after delivery failure. eventId={}, email={}, reason={}",
                    event.getEventId(),
                    email,
                    e.getMessage());
        } catch (RuntimeException e) {
            logger.warn(
                    "Skip email verification email after unexpected delivery failure. eventId={}, email={}",
                    event.getEventId(),
                    email,
                    e);
        }
    }

    @Override
    public void sendSagaCheckoutNotification(String eventType, CheckoutNotificationPayload payload) {
        UUID sagaId = payload.getSagaId();
        UUID orderId = payload.getOrderId();
        logger.info(
                "Processing saga notification: eventType={}, sagaId={}, orderId={}, userId={}",
                eventType,
                sagaId,
                orderId,
                payload.getUserId());

        String email = resolveRecipientEmail(payload.getCustomerEmail(), payload.getUserId());
        String customerName = payload.getCustomerName();
        String sagaIdStr = sagaId != null ? sagaId.toString() : null;
        String orderIdStr = orderId != null ? orderId.toString() : null;

        String normalizedType = eventType != null ? eventType.trim() : "";
        String inner;
        String subject;

        switch (normalizedType) {
            case SagaEventTypes.CHECKOUT_COMPLETED -> {
                String totalStr = payload.getTotalPrice() != null ? payload.getTotalPrice().toPlainString() : null;
                inner = BookstoreNotificationHtmlBuilder.buildCheckoutCompletedBody(
                        customerName, sagaIdStr, orderIdStr, totalStr);
                subject = "Đặt hàng thành công" + orderSuffix(orderIdStr);
            }
            case SagaEventTypes.CHECKOUT_FAILED -> {
                inner = BookstoreNotificationHtmlBuilder.buildCheckoutFailedBody(
                        customerName, sagaIdStr, orderIdStr, payload.getFailureReason());
                subject = "Checkout thất bại" + orderSuffix(orderIdStr);
            }
            case SagaEventTypes.PAYMENT_COMPLETED -> {
                String amountStr = payload.getAmount() != null ? payload.getAmount().toPlainString() : null;
                String currency = payload.getCurrency() != null && !payload.getCurrency().isBlank()
                        ? payload.getCurrency()
                        : "VND";
                String paymentIdStr =
                        payload.getPaymentId() != null ? payload.getPaymentId().toString() : null;
                inner = BookstoreNotificationHtmlBuilder.buildPaymentCompletedSagaBody(
                        customerName,
                        sagaIdStr,
                        orderIdStr,
                        paymentIdStr,
                        amountStr,
                        currency,
                        payload.getPaymentMethod());
                subject = "Thanh toán thành công" + orderSuffix(orderIdStr);
            }
            case SagaEventTypes.ORDER_CANCELLED -> {
                String reason = payload.getFailureReason() != null
                        ? payload.getFailureReason()
                        : payload.getStatus();
                inner = BookstoreNotificationHtmlBuilder.buildOrderCancelledBody(
                        customerName, sagaIdStr, orderIdStr, reason);
                subject = "Đơn hàng đã bị hủy" + orderSuffix(orderIdStr);
            }
            default -> {
                logger.warn(
                        "Unknown saga event type={}, sagaId={}, orderId={} — skipping email",
                        eventType,
                        sagaId,
                        orderId);
                return;
            }
        }

        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(subject, inner);
        logger.debug("Saga notification HTML prepared: sagaId={}, orderId={}, subject={}", sagaId, orderId, subject);
        mailDeliveryService.sendHtmlEmail(email, subject, html, sagaId, orderId);
    }

    private static String orderSuffix(String orderIdStr) {
        return orderIdStr != null ? " — đơn #" + orderIdStr : "";
    }

    @Override
    public void sendEmailFallback(NotificationRequestDto request) {
        logger.info("Processing Fallback Notification to: {}", request.getTo());
        String inner = BookstoreNotificationHtmlBuilder.escapeWithLineBreaks(request.getMessage());
        String subject = request.getSubject() != null ? request.getSubject() : "Thông báo";
        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(subject, inner);
        logger.info("Subject: {}. HTML email prepared ({} chars).", subject, html.length());

        mailDeliveryService.sendHtmlEmail(request.getTo(), subject, html, null, null);
    }

    private String resolveRecipientEmail(String directEmail, UUID userId) {
        if (StringUtils.hasText(directEmail)) {
            return directEmail.trim();
        }
        if (userId != null) {
            UserContactInfoResponse contactInfo = userContactResolver.resolveContact(userId);
            if (contactInfo == null || !StringUtils.hasText(contactInfo.getEmail())) {
                logger.warn("Không lấy được contact-info hợp lệ từ user-service. userId={}", userId);
                return null;
            }
            logger.info("Fetched contact-info from user-service. userId={}, email={}, phoneNumber={}, deviceToken={}",
                    contactInfo != null ? contactInfo.getUserId() : null,
                    contactInfo != null ? contactInfo.getEmail() : null,
                    contactInfo != null ? contactInfo.getPhoneNumber() : null,
                    contactInfo != null ? contactInfo.getDeviceToken() : null);
            return contactInfo != null ? contactInfo.getEmail() : null;
        }
        return null;
    }

}
