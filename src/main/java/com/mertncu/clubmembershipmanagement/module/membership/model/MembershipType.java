package com.mertncu.clubmembershipmanagement.module.membership.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;

public class MembershipType extends BaseEntity {
    private String name;
    private int durationMonths;
    private double price;

    public MembershipType() {}

    public MembershipType(String name, int durationMonths, double price) {
        this.name = name;
        this.durationMonths = durationMonths;
        this.price = price;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
