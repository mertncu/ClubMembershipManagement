package com.mertncu.clubmembershipmanagement.module.diet.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDate;

/** A complete personalised diet plan assigned by a trainer to a member. */
public class DietProgram extends BaseEntity {
    private int       userId;        // member who follows the plan
    private int       trainerId;     // trainer who created it
    private String    name;
    private String    goal;          // e.g. "Weight loss", "Muscle gain"
    private LocalDate startDate;
    private LocalDate endDate;
    private int       dailyCalories;
    private String    notes;

    public DietProgram() {}

    public DietProgram(int userId, int trainerId, String name, String goal,
                       LocalDate startDate, LocalDate endDate, int dailyCalories, String notes) {
        this.userId        = userId;
        this.trainerId     = trainerId;
        this.name          = name;
        this.goal          = goal;
        this.startDate     = startDate;
        this.endDate       = endDate;
        this.dailyCalories = dailyCalories;
        this.notes         = notes;
    }

    public int       getUserId()              { return userId; }
    public void      setUserId(int v)         { this.userId = v; }
    public int       getTrainerId()           { return trainerId; }
    public void      setTrainerId(int v)      { this.trainerId = v; }
    public String    getName()                { return name; }
    public void      setName(String v)        { this.name = v; }
    public String    getGoal()                { return goal; }
    public void      setGoal(String v)        { this.goal = v; }
    public LocalDate getStartDate()           { return startDate; }
    public void      setStartDate(LocalDate v){ this.startDate = v; }
    public LocalDate getEndDate()             { return endDate; }
    public void      setEndDate(LocalDate v)  { this.endDate = v; }
    public int       getDailyCalories()       { return dailyCalories; }
    public void      setDailyCalories(int v)  { this.dailyCalories = v; }
    public String    getNotes()               { return notes; }
    public void      setNotes(String v)       { this.notes = v; }
}
