package com.mertncu.clubmembershipmanagement.module.payment.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDate;

public class Coupon extends BaseEntity {
    private String code;
    private double discountPercent; // e.g. 20.0 = %20 off
    private LocalDate validUntil;
    private boolean isActive;
    private String description;

    public Coupon() {}

    public Coupon(String code, double discountPercent, LocalDate validUntil, String description) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.validUntil = validUntil;
        this.isActive = true;
        this.description = description;
    }

    public boolean isValid() {
        return isActive && (validUntil == null || !validUntil.isBefore(LocalDate.now()));
    }

    public String getCode()                   { return code; }
    public void setCode(String v)             { this.code = v; }
    public double getDiscountPercent()        { return discountPercent; }
    public void setDiscountPercent(double v)  { this.discountPercent = v; }
    public LocalDate getValidUntil()          { return validUntil; }
    public void setValidUntil(LocalDate v)    { this.validUntil = v; }
    public boolean isActive()                 { return isActive; }
    public void setActive(boolean v)          { this.isActive = v; }
    public String getDescription()            { return description; }
    public void setDescription(String v)      { this.description = v; }
}
