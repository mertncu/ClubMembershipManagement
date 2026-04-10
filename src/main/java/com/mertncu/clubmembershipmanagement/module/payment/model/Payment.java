package com.mertncu.clubmembershipmanagement.module.payment.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDateTime;

public class Payment extends BaseEntity {
    private int userId;
    private int subscriptionId;
    private double amount;
    private double discountAmount;
    private Integer couponId; // nullable
    private String paymentMethod; // CREDIT_CARD, CASH, BANK_TRANSFER
    private String status;       // COMPLETED, PENDING, FAILED
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(int userId, int subscriptionId, double amount,
                   double discountAmount, Integer couponId,
                   String paymentMethod, String status) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.discountAmount = discountAmount;
        this.couponId = couponId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paidAt = LocalDateTime.now();
    }

    public int getUserId()                   { return userId; }
    public void setUserId(int v)             { this.userId = v; }
    public int getSubscriptionId()           { return subscriptionId; }
    public void setSubscriptionId(int v)     { this.subscriptionId = v; }
    public double getAmount()                { return amount; }
    public void setAmount(double v)          { this.amount = v; }
    public double getDiscountAmount()        { return discountAmount; }
    public void setDiscountAmount(double v)  { this.discountAmount = v; }
    public Integer getCouponId()             { return couponId; }
    public void setCouponId(Integer v)       { this.couponId = v; }
    public String getPaymentMethod()         { return paymentMethod; }
    public void setPaymentMethod(String v)   { this.paymentMethod = v; }
    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }
    public LocalDateTime getPaidAt()         { return paidAt; }
    public void setPaidAt(LocalDateTime v)   { this.paidAt = v; }
}
