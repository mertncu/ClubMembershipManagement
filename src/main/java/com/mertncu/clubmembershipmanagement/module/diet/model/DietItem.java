package com.mertncu.clubmembershipmanagement.module.diet.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;

/** A single meal entry within a DietProgram. */
public class DietItem extends BaseEntity {
    private int    programId;
    private String mealType;   // BREAKFAST, LUNCH, DINNER, SNACK
    private String foodName;
    private double quantity;   // grams or ml
    private int    calories;
    private double protein;    // grams
    private double carbs;      // grams
    private double fat;        // grams
    private String dayOfWeek;  // MON, TUE … or ALL

    public DietItem() {}

    public DietItem(int programId, String mealType, String foodName,
                    double quantity, int calories, double protein, double carbs, double fat, String dayOfWeek) {
        this.programId  = programId;
        this.mealType   = mealType;
        this.foodName   = foodName;
        this.quantity   = quantity;
        this.calories   = calories;
        this.protein    = protein;
        this.carbs      = carbs;
        this.fat        = fat;
        this.dayOfWeek  = dayOfWeek;
    }

    public int    getProgramId()          { return programId; }
    public void   setProgramId(int v)     { this.programId = v; }
    public String getMealType()           { return mealType; }
    public void   setMealType(String v)   { this.mealType = v; }
    public String getFoodName()           { return foodName; }
    public void   setFoodName(String v)   { this.foodName = v; }
    public double getQuantity()           { return quantity; }
    public void   setQuantity(double v)   { this.quantity = v; }
    public int    getCalories()           { return calories; }
    public void   setCalories(int v)      { this.calories = v; }
    public double getProtein()            { return protein; }
    public void   setProtein(double v)    { this.protein = v; }
    public double getCarbs()              { return carbs; }
    public void   setCarbs(double v)      { this.carbs = v; }
    public double getFat()                { return fat; }
    public void   setFat(double v)        { this.fat = v; }
    public String getDayOfWeek()          { return dayOfWeek; }
    public void   setDayOfWeek(String v)  { this.dayOfWeek = v; }
}
