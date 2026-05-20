package com.notfound.bookstorenotificationservice.exception;

/**
 * Lỗi gửi email — listener ném exception để RabbitMQ retry / đưa vào DLQ.
 * Không publish event ngược về saga.
 */
public class NotificationDeliveryException extends RuntimeException {

    public NotificationDeliveryException(String message) {
        super(message);
    }

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
