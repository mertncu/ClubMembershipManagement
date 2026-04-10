package com.mertncu.clubmembershipmanagement.module.body.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDate;

public class BodyMeasurement extends BaseEntity {
    private int userId;
    private double weightKg;
    private double heightCm;
    private int age;
    private String gender; // MALE, FEMALE
    private double bmi;
    private double bodyFatPercent;
    private LocalDate measuredAt;

    public BodyMeasurement() {}

    public BodyMeasurement(int userId, double weightKg, double heightCm, int age, String gender) {
        this.userId = userId;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.age = age;
        this.gender = gender;
        this.measuredAt = LocalDate.now();
        this.bmi = calcBMI();
        this.bodyFatPercent = calcBodyFat();
    }

    /** BMI = weight(kg) / height(m)^2 */
    public double calcBMI() {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /** Deurenberg formula: BF% = (1.2 * BMI) + (0.23 * age) - (10.8 * sexFactor) - 5.4 */
    public double calcBodyFat() {
        double bmiVal = calcBMI();
        double sexFactor = "MALE".equalsIgnoreCase(gender) ? 1.0 : 0.0;
        return (1.2 * bmiVal) + (0.23 * age) - (10.8 * sexFactor) - 5.4;
    }

    /** Returns BMI category label */
    public static String bmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    public int getUserId()                 { return userId; }
    public void setUserId(int v)           { this.userId = v; }
    public double getWeightKg()            { return weightKg; }
    public void setWeightKg(double v)      { this.weightKg = v; }
    public double getHeightCm()            { return heightCm; }
    public void setHeightCm(double v)      { this.heightCm = v; }
    public int getAge()                    { return age; }
    public void setAge(int v)              { this.age = v; }
    public String getGender()              { return gender; }
    public void setGender(String v)        { this.gender = v; }
    public double getBmi()                 { return bmi; }
    public void setBmi(double v)           { this.bmi = v; }
    public double getBodyFatPercent()      { return bodyFatPercent; }
    public void setBodyFatPercent(double v){ this.bodyFatPercent = v; }
    public LocalDate getMeasuredAt()       { return measuredAt; }
    public void setMeasuredAt(LocalDate v) { this.measuredAt = v; }
}
