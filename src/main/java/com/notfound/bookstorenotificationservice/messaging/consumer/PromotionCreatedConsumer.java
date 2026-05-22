package com.notfound.bookstorenotificationservice.messaging.consumer;

import com.notfound.bookstorenotificationservice.config.RabbitMQConfig;
import com.notfound.bookstorenotificationservice.messaging.PromotionEventParser;
import com.notfound.bookstorenotificationservice.messaging.PromotionEventTypes;
import com.notfound.bookstorenotificationservice.model.dto.PromotionCreatedEvent;
import com.notfound.bookstorenotificationservice.notification.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PromotionCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PromotionCreatedConsumer.class);

    private final PromotionEventParser promotionEventParser;
    private final NotificationDispatcher notificationDispatcher;

    public PromotionCreatedConsumer(
            PromotionEventParser promotionEventParser,
            NotificationDispatcher notificationDispatcher) {
        this.promotionEventParser = promotionEventParser;
        this.notificationDispatcher = notificationDispatcher;
    }

    @RabbitListener(
            queues = RabbitMQConfig.PROMOTION_CREATED_QUEUE_NAME,
            containerFactory = "rawRabbitListenerContainerFactory")
    public void handlePromotionCreated(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        PromotionCreatedEvent event = promotionEventParser.parsePromotionCreated(message.getBody());
        logger.info(
                "Received promotion event: routingKey={}, promotionId={}, code={}, status={}",
                routingKey,
                event.getPromotionId(),
                event.getCode(),
                event.getStatus());
        notificationDispatcher.deliver(PromotionEventTypes.PROMOTION_CREATED, event);
    }
}
