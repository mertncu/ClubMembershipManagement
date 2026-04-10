package com.mertncu.clubmembershipmanagement.module.workout.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;

/** A single exercise entry inside a WorkoutProgram. */
public class WorkoutExercise extends BaseEntity {
    private int    programId;
    private String name;
    private String muscleGroup;   // CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, CARDIO
    private int    sets;
    private int    reps;
    private double weightKg;      // 0 = bodyweight
    private int    restSeconds;
    private String dayOfWeek;     // MON, TUE … or ALL
    private String notes;

    public WorkoutExercise() {}

    public WorkoutExercise(int programId, String name, String muscleGroup,
                           int sets, int reps, double weightKg, int restSeconds,
                           String dayOfWeek, String notes) {
        this.programId   = programId;
        this.name        = name;
        this.muscleGroup = muscleGroup;
        this.sets        = sets;
        this.reps        = reps;
        this.weightKg    = weightKg;
        this.restSeconds = restSeconds;
        this.dayOfWeek   = dayOfWeek;
        this.notes       = notes;
    }

    public int    getProgramId()          { return programId; }
    public void   setProgramId(int v)     { this.programId = v; }
    public String getName()               { return name; }
    public void   setName(String v)       { this.name = v; }
    public String getMuscleGroup()        { return muscleGroup; }
    public void   setMuscleGroup(String v){ this.muscleGroup = v; }
    public int    getSets()               { return sets; }
    public void   setSets(int v)          { this.sets = v; }
    public int    getReps()               { return reps; }
    public void   setReps(int v)          { this.reps = v; }
    public double getWeightKg()           { return weightKg; }
    public void   setWeightKg(double v)   { this.weightKg = v; }
    public int    getRestSeconds()        { return restSeconds; }
    public void   setRestSeconds(int v)   { this.restSeconds = v; }
    public String getDayOfWeek()          { return dayOfWeek; }
    public void   setDayOfWeek(String v)  { this.dayOfWeek = v; }
    public String getNotes()              { return notes; }
    public void   setNotes(String v)      { this.notes = v; }
}
