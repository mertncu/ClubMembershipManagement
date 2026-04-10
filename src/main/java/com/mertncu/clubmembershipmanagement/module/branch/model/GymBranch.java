package com.mertncu.clubmembershipmanagement.module.branch.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;

public class GymBranch extends BaseEntity {
    private String name;
    private String address;
    private String city;
    private String phone;
    private String managerName;
    private int capacity;

    public GymBranch() {}

    public GymBranch(String name, String address, String city,
                     String phone, String managerName, int capacity) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.managerName = managerName;
        this.capacity = capacity;
    }

    public String getName()               { return name; }
    public void setName(String v)         { this.name = v; }
    public String getAddress()            { return address; }
    public void setAddress(String v)      { this.address = v; }
    public String getCity()               { return city; }
    public void setCity(String v)         { this.city = v; }
    public String getPhone()              { return phone; }
    public void setPhone(String v)        { this.phone = v; }
    public String getManagerName()        { return managerName; }
    public void setManagerName(String v)  { this.managerName = v; }
    public int getCapacity()              { return capacity; }
    public void setCapacity(int v)        { this.capacity = v; }
}
