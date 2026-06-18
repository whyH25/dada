package com.ssafy.mvc.dto;

import java.time.LocalDateTime;

public class PaymentDto {
    private Long paymentId;
    private Long userId;
    private String orderId;
    private String paymentKey;
    private String planName;
    private int amount;
    private int tickets;
    private String status;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPaymentKey() { return paymentKey; }
    public void setPaymentKey(String paymentKey) { this.paymentKey = paymentKey; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public int getTickets() { return tickets; }
    public void setTickets(int tickets) { this.tickets = tickets; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
