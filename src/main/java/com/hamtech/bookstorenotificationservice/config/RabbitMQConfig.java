package com.hamtech.bookstorenotificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bookstore.events";
    public static final String ORDER_QUEUE_NAME = "notification.order_events";
    public static final String PAYMENT_QUEUE_NAME = "notification.payment_events";
    public static final String ORDER_ROUTING_KEY = "order.#";
    public static final String PAYMENT_ROUTING_KEY = "payment.#";

    @Bean
    public TopicExchange bookstoreExchange() {
        return new TopicExchange(EXCHANGE_NAME);
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
    public Binding orderBinding(Queue orderQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(orderQueue).to(bookstoreExchange).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(paymentQueue).to(bookstoreExchange).with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
