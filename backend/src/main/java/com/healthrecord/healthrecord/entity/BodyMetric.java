package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "body_metrics")
public class BodyMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double heightInCm;
    private Double weightInKg;
    private LocalDateTime logDate;
    private Double bmi;

    
    @Column(name = "systolic_bp")
    private Integer systolicBp; 

    @Column(name = "diastolic_bp")
    private Integer diastolicBp; 

    
    private Integer cholesterol;

    
    private Integer glucose;

    private Boolean smoking = false; 
    private Boolean alcohol = false; 
    private Boolean physicalActivity = true; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private HealthProfile healthProfile;

    @PrePersist
    protected void onPersist() {
        if (this.logDate == null) {
            this.logDate = LocalDateTime.now();
        }
        this.calculateBmi();
        
        if(this.cholesterol == null) this.cholesterol = 1;
        if(this.glucose == null) this.glucose = 1;
        if(this.smoking == null) this.smoking = false;
        if(this.alcohol == null) this.alcohol = false;
        if(this.physicalActivity == null) this.physicalActivity = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.calculateBmi();
    }

    public void calculateBmi() {
        if (this.heightInCm != null && this.heightInCm > 0 && this.weightInKg != null && this.weightInKg > 0) {
            double heightInM = this.heightInCm / 100.0;
            this.bmi = this.weightInKg / (heightInM * heightInM);
        } else {
            this.bmi = null;
        }
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getHeightInCm() { return heightInCm; }
    public void setHeightInCm(Double heightInCm) { this.heightInCm = heightInCm; }

    public Double getWeightInKg() { return weightInKg; }
    public void setWeightInKg(Double weightInKg) { this.weightInKg = weightInKg; }

    public LocalDateTime getLogDate() { return logDate; }
    public void setLogDate(LocalDateTime logDate) { this.logDate = logDate; }

    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }

    
    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }

    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }

    public Integer getCholesterol() { return cholesterol; }
    public void setCholesterol(Integer cholesterol) { this.cholesterol = cholesterol; }

    public Integer getGlucose() { return glucose; }
    public void setGlucose(Integer glucose) { this.glucose = glucose; }

    public Boolean getSmoking() { return smoking; }
    public void setSmoking(Boolean smoking) { this.smoking = smoking; }

    public Boolean getAlcohol() { return alcohol; }
    public void setAlcohol(Boolean alcohol) { this.alcohol = alcohol; }

    public Boolean getPhysicalActivity() { return physicalActivity; }
    public void setPhysicalActivity(Boolean physicalActivity) { this.physicalActivity = physicalActivity; }

    public HealthProfile getHealthProfile() { return healthProfile; }
    public void setHealthProfile(HealthProfile healthProfile) { this.healthProfile = healthProfile; }
}