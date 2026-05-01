package com.hamtech.bookstorenotificationservice.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderEventDto {
    private UUID orderId;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalPrice;
    private String status;

    // Constructors, Getters and Setters

    public OrderEventDto() {}

    public OrderEventDto(UUID orderId, String customerEmail, String customerName, BigDecimal totalPrice, String status) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OrderEventDto{" +
                "orderId=" + orderId +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }
}
