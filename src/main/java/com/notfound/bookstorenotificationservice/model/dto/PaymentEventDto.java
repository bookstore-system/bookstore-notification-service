package com.notfound.bookstorenotificationservice.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Sự kiện thanh toán từ payment-service: cấu trúc khác order — tập trung giao dịch, phương thức, kết quả thanh toán.
 */
public class PaymentEventDto {

    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private String customerEmail;
    private String customerName;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;

    public PaymentEventDto() {}

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return "PaymentEventDto{"
                + "paymentId=" + paymentId
                + ", orderId=" + orderId
                + ", userId=" + userId
                + ", customerEmail='" + (customerEmail != null ? "***" : null) + '\''
                + ", customerName='" + customerName + '\''
                + ", amount=" + amount
                + ", currency='" + currency + '\''
                + ", status='" + status + '\''
                + ", paymentMethod='" + paymentMethod + '\''
                + '}';
    }
}
