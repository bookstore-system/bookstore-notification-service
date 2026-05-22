package com.notfound.bookstorenotificationservice.service.impl;

import com.notfound.bookstorenotificationservice.client.ApiResponse;
import com.notfound.bookstorenotificationservice.client.UserContactInfoResponse;
import com.notfound.bookstorenotificationservice.client.UserServiceClient;
import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import com.notfound.bookstorenotificationservice.messaging.SagaEventTypes;
import com.notfound.bookstorenotificationservice.model.dto.CheckoutNotificationPayload;
import com.notfound.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.notfound.bookstorenotificationservice.model.dto.OrderEventDto;
import com.notfound.bookstorenotificationservice.model.dto.PasswordResetEventDto;
import com.notfound.bookstorenotificationservice.model.dto.PaymentEventDto;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import com.notfound.bookstorenotificationservice.util.BookstoreNotificationHtmlBuilder;
import java.util.UUID;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final UserServiceClient userServiceClient;
    private final JavaMailSender javaMailSender;
    private final String mailFrom;

    public NotificationServiceImpl(
            UserServiceClient userServiceClient,
            @Autowired(required = false) JavaMailSender javaMailSender,
            @Value("${notification.mail.from:no-reply@localhost}") String mailFrom) {
        this.userServiceClient = userServiceClient;
        this.javaMailSender = javaMailSender;
        this.mailFrom = mailFrom;
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
        logger.debug("Bookstore order notification HTML:\n{}", html);

        sendHtmlEmail(email, subject, html, null, orderEvent.getOrderId());
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
        logger.debug("Bookstore payment notification HTML:\n{}", html);

        sendHtmlEmail(email, subject, html, null, paymentEvent.getOrderId());
    }

    @Override
    public void sendPasswordResetNotification(PasswordResetEventDto event) {
        logger.info("Processing password-reset notification for userId={}", event.getUserId());
        if (!StringUtils.hasText(event.getResetLink())) {
            logger.warn("Bỏ qua email đặt lại mật khẩu: thiếu resetLink.");
            return;
        }

        String email = resolveRecipientEmail(event.getEmail(), event.getUserId());
        int expires = event.getExpiresInMinutes() != null && event.getExpiresInMinutes() > 0
                ? event.getExpiresInMinutes()
                : 60;
        String safeHref = escapeForHtmlAttribute(event.getResetLink().trim());
        String inner = BookstoreNotificationHtmlBuilder.buildPasswordResetBody(
                event.getDisplayName(), safeHref, expires);
        String subject = "Đặt lại mật khẩu — NotFound Bookstore";
        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(subject, inner);

        logger.info("Password-reset email prepared for {} (HTML {} chars). expiresInMinutes={}",
                email, html.length(), expires);
        logger.debug("Bookstore password-reset notification HTML:\n{}", html);

        sendHtmlEmail(email, subject, html, null, null);
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
        sendHtmlEmail(email, subject, html, sagaId, orderId);
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
        logger.debug("Bookstore fallback notification HTML:\n{}", html);

        sendHtmlEmail(request.getTo(), subject, html, null, null);
    }

    private String resolveRecipientEmail(String directEmail, UUID userId) {
        if (StringUtils.hasText(directEmail)) {
            return directEmail.trim();
        }
        if (userId != null) {
            ApiResponse<UserContactInfoResponse> response = userServiceClient.getUserContactInfo(userId);
            UserContactInfoResponse contactInfo = response != null ? response.getResult() : null;
            if (contactInfo == null || !StringUtils.hasText(contactInfo.getEmail())) {
                logger.warn("Không lấy được contact-info hợp lệ từ user-service. userId={}", userId);
                return null;
            }
            logger.info("Fetched contact-info from user-service. userId={}, email={}, phoneNumber={}, deviceToken={}",
                    contactInfo.getUserId(),
                    contactInfo.getEmail(),
                    contactInfo.getPhoneNumber(),
                    contactInfo.getDeviceToken());
            return contactInfo.getEmail();
        }
        return null;
    }

    /** Giá trị an toàn cho thuộc tính HTML {@code href} (không log ra console). */
    private static String escapeForHtmlAttribute(String url) {
        if (url == null) {
            return "";
        }
        return url.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody, UUID sagaId, UUID orderId) {
        if (javaMailSender == null) {
            logger.warn(
                    "Không gửi được email: chưa cấu hình JavaMailSender (sagaId={}, orderId={}).",
                    sagaId,
                    orderId);
            return;
        }
        if (!StringUtils.hasText(to)) {
            logger.warn("Không gửi email: thiếu địa chỉ người nhận (sagaId={}, orderId={}).", sagaId, orderId);
            return;
        }
        if (!StringUtils.hasText(mailFrom)) {
            logger.warn(
                    "Không gửi email: thiếu notification.mail.from (sagaId={}, orderId={}).", sagaId, orderId);
            return;
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to.trim());
            helper.setSubject(subject != null ? subject : "Thông báo");
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            logger.info("Đã gửi email HTML tới {} (sagaId={}, orderId={})", to, sagaId, orderId);
        } catch (MessagingException e) {
            logger.error(
                    "Lỗi tạo email gửi tới {} (sagaId={}, orderId={}): {}",
                    to,
                    sagaId,
                    orderId,
                    e.getMessage());
            throw new NotificationDeliveryException("Failed to build email", e);
        } catch (org.springframework.mail.MailException e) {
            logger.error(
                    "Lỗi SMTP gửi tới {} (sagaId={}, orderId={}): {}",
                    to,
                    sagaId,
                    orderId,
                    e.getMessage(),
                    e);
            throw new NotificationDeliveryException("Failed to send email", e);
        }
    }
}
