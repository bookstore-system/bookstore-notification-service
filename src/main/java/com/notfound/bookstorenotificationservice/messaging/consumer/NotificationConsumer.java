package com.notfound.bookstorenotificationservice.messaging.consumer;

import com.notfound.bookstorenotificationservice.config.RabbitMQConfig;
import com.notfound.bookstorenotificationservice.model.dto.EmailVerificationEvent;
import com.notfound.bookstorenotificationservice.model.dto.OrderEventDto;
import com.notfound.bookstorenotificationservice.model.dto.PasswordResetOtpEvent;
import com.notfound.bookstorenotificationservice.model.dto.PaymentEventDto;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderEvent(OrderEventDto orderEvent) {
        notificationService.sendOrderNotification(orderEvent);
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void handlePaymentEvent(PaymentEventDto paymentEvent) {
        notificationService.sendPaymentNotification(paymentEvent);
    }

    @RabbitListener(queues = RabbitMQConfig.USER_QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void handlePasswordResetEvent(PasswordResetOtpEvent event) {
        notificationService.sendPasswordResetOtpNotification(event);
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_VERIFICATION_QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void handleEmailVerificationEvent(EmailVerificationEvent event) {
        notificationService.sendEmailVerificationNotification(event);
    }
}
