package com.mertncu.clubmembershipmanagement.module.body.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;

/**
 * Extended profile for TRAINER users — linked by user_id.
 */
public class TrainerProfile extends BaseEntity {
    private int    userId;
    private String specialization;   // e.g. "Strength & Conditioning"
    private String bio;
    private int    yearsExperience;
    private String certifications;   // comma-separated
    private String photoUrl;         // optional future use

    public TrainerProfile() {}

    public TrainerProfile(int userId, String specialization, String bio,
                          int yearsExperience, String certifications) {
        this.userId          = userId;
        this.specialization  = specialization;
        this.bio             = bio;
        this.yearsExperience = yearsExperience;
        this.certifications  = certifications;
    }

    public int    getUserId()               { return userId; }
    public void   setUserId(int v)          { this.userId = v; }
    public String getSpecialization()       { return specialization; }
    public void   setSpecialization(String v){ this.specialization = v; }
    public String getBio()                  { return bio; }
    public void   setBio(String v)          { this.bio = v; }
    public int    getYearsExperience()      { return yearsExperience; }
    public void   setYearsExperience(int v) { this.yearsExperience = v; }
    public String getCertifications()       { return certifications; }
    public void   setCertifications(String v){ this.certifications = v; }
    public String getPhotoUrl()             { return photoUrl; }
    public void   setPhotoUrl(String v)     { this.photoUrl = v; }
}
