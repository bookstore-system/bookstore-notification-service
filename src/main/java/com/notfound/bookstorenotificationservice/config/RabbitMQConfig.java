package com.notfound.bookstorenotificationservice.config;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstorenotificationservice.messaging.SagaEventTypes;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bookstore.events";

    public static final String SAGA_EVENTS_QUEUE_NAME = "notification.events.queue";
    public static final String SAGA_EVENTS_DLQ_NAME = "notification.events.dlq";

    public static final String ORDER_QUEUE_NAME = "notification.order_events";
    public static final String PAYMENT_QUEUE_NAME = "notification.payment_events";
    public static final String USER_QUEUE_NAME = "notification.password_reset_events";
    public static final String EMAIL_VERIFICATION_QUEUE_NAME = "notification.email_verification_events";
    public static final String PROMOTION_CREATED_QUEUE_NAME = "notification.promotion_created";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String PASSWORD_RESET_ROUTING_KEY = "user.password_reset";
    public static final String EMAIL_VERIFICATION_ROUTING_KEY = "user.email_verification";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String PROMOTION_CREATED_ROUTING_KEY = "promotion.created";

    @Value("${notification.rabbit.retry.max-attempts:3}")
    private int sagaRetryMaxAttempts;

    @Value("${notification.rabbit.retry.initial-interval-ms:2000}")
    private long sagaRetryInitialIntervalMs;

    @Value("${notification.rabbit.retry.multiplier:2.0}")
    private double sagaRetryMultiplier;

    @Value("${notification.rabbit.retry.max-interval-ms:10000}")
    private long sagaRetryMaxIntervalMs;

    @Bean
    public TopicExchange bookstoreExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue sagaEventsQueue() {
        return QueueBuilder.durable(SAGA_EVENTS_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", SAGA_EVENTS_DLQ_NAME)
                .build();
    }

    @Bean
    public Queue sagaEventsDlq() {
        return new Queue(SAGA_EVENTS_DLQ_NAME, true);
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE_NAME, true);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE_NAME, true);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE_NAME, true);
    }

    @Bean
    public Queue emailVerificationQueue() {
        return new Queue(EMAIL_VERIFICATION_QUEUE_NAME, true);
    }

    @Bean
    public Queue promotionCreatedQueue() {
        return new Queue(PROMOTION_CREATED_QUEUE_NAME, true);
    }

    @Bean
    public Binding sagaCheckoutCompletedBinding(Queue sagaEventsQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(sagaEventsQueue).to(bookstoreExchange).with(SagaEventTypes.CHECKOUT_COMPLETED);
    }

    @Bean
    public Binding sagaCheckoutFailedBinding(Queue sagaEventsQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(sagaEventsQueue).to(bookstoreExchange).with(SagaEventTypes.CHECKOUT_FAILED);
    }

    @Bean
    public Binding sagaPaymentCompletedBinding(Queue sagaEventsQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(sagaEventsQueue).to(bookstoreExchange).with(SagaEventTypes.PAYMENT_COMPLETED);
    }

    @Bean
    public Binding sagaOrderCancelledBinding(Queue sagaEventsQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(sagaEventsQueue).to(bookstoreExchange).with(SagaEventTypes.ORDER_CANCELLED);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(orderQueue).to(bookstoreExchange).with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding orderConfirmedBinding(Queue orderQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(orderQueue).to(bookstoreExchange).with(ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(paymentQueue).to(bookstoreExchange).with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding userBinding(Queue userQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(userQueue).to(bookstoreExchange).with(PASSWORD_RESET_ROUTING_KEY);
    }

    @Bean
    public Binding emailVerificationBinding(Queue emailVerificationQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(emailVerificationQueue).to(bookstoreExchange).with(EMAIL_VERIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding promotionCreatedBinding(Queue promotionCreatedQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(promotionCreatedQueue).to(bookstoreExchange).with(PROMOTION_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature(), true)
                .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true)
                .findAndRegisterModules();
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rawRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor sagaRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(sagaRetryMaxAttempts)
                .backOffOptions(sagaRetryInitialIntervalMs, sagaRetryMultiplier, sagaRetryMaxIntervalMs)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory sagaRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor sagaRetryInterceptor
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        factory.setAdviceChain(sagaRetryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
