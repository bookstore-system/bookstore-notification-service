package com.notfound.bookstorenotificationservice.messaging;

public final class SagaEventTypes {

    public static final String CHECKOUT_COMPLETED = "checkout.completed";
    public static final String CHECKOUT_FAILED = "checkout.failed";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String ORDER_CANCELLED = "order.cancelled";

    private SagaEventTypes() {}
}
