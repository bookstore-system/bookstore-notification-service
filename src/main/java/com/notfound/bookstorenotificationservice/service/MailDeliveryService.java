package com.notfound.bookstorenotificationservice.service;

import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(MailDeliveryService.class);

    private final JavaMailSender javaMailSender;
    private final String mailFrom;

    public MailDeliveryService(
            @Autowired(required = false) JavaMailSender javaMailSender,
            @Value("${notification.mail.from:no-reply@localhost}") String mailFrom) {
        this.javaMailSender = javaMailSender;
        this.mailFrom = mailFrom;
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody, UUID sagaId, UUID orderId) {
        sendHtmlEmail(to, subject, htmlBody, sagaId, orderId, null);
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody, String context) {
        sendHtmlEmail(to, subject, htmlBody, null, null, context);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody, UUID sagaId, UUID orderId, String context) {
        String logContext = buildLogContext(context, sagaId, orderId);
        if (javaMailSender == null) {
            logger.warn("Cannot send email: JavaMailSender is not configured {}.", logContext);
            return;
        }
        if (!StringUtils.hasText(to)) {
            logger.warn("Cannot send email: missing recipient address {}.", logContext);
            return;
        }
        if (!StringUtils.hasText(mailFrom)) {
            logger.warn("Cannot send email: missing notification.mail.from {}.", logContext);
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
            logger.info("Sent HTML email to {} {}", to, logContext);
        } catch (MessagingException e) {
            logger.error(
                    "Failed to build email to {} {}: {}",
                    to,
                    logContext,
                    e.getMessage());
            throw new NotificationDeliveryException("Failed to build email", e);
        } catch (org.springframework.mail.MailException e) {
            logger.error(
                    "SMTP error sending to {} {}: {}",
                    to,
                    logContext,
                    e.getMessage(),
                    e);
            throw new NotificationDeliveryException("Failed to send email", e);
        }
    }

    private String buildLogContext(String context, UUID sagaId, UUID orderId) {
        if (StringUtils.hasText(context)) {
            return "(context=" + context.trim() + ")";
        }
        return "(sagaId=" + sagaId + ", orderId=" + orderId + ")";
    }
}
