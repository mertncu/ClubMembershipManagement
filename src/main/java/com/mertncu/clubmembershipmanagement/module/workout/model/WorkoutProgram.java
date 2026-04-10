package com.mertncu.clubmembershipmanagement.module.workout.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDate;

/** A personalised workout plan assigned to a member by a trainer. */
public class WorkoutProgram extends BaseEntity {
    private int       userId;
    private int       trainerId;
    private String    name;
    private String    goal;           // e.g. "Build muscle", "Cardio endurance"
    private String    level;          // BEGINNER, INTERMEDIATE, ADVANCED
    private int       durationWeeks;
    private LocalDate startDate;
    private String    notes;

    public WorkoutProgram() {}

    public WorkoutProgram(int userId, int trainerId, String name, String goal,
                          String level, int durationWeeks, LocalDate startDate, String notes) {
        this.userId         = userId;
        this.trainerId      = trainerId;
        this.name           = name;
        this.goal           = goal;
        this.level          = level;
        this.durationWeeks  = durationWeeks;
        this.startDate      = startDate;
        this.notes          = notes;
    }

    public int       getUserId()               { return userId; }
    public void      setUserId(int v)          { this.userId = v; }
    public int       getTrainerId()            { return trainerId; }
    public void      setTrainerId(int v)       { this.trainerId = v; }
    public String    getName()                 { return name; }
    public void      setName(String v)         { this.name = v; }
    public String    getGoal()                 { return goal; }
    public void      setGoal(String v)         { this.goal = v; }
    public String    getLevel()                { return level; }
    public void      setLevel(String v)        { this.level = v; }
    public int       getDurationWeeks()        { return durationWeeks; }
    public void      setDurationWeeks(int v)   { this.durationWeeks = v; }
    public LocalDate getStartDate()            { return startDate; }
    public void      setStartDate(LocalDate v) { this.startDate = v; }
    public String    getNotes()                { return notes; }
    public void      setNotes(String v)        { this.notes = v; }
}
