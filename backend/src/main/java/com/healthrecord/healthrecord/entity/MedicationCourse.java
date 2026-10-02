package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medication_courses")
public class MedicationCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_item_id")
    private PrescriptionItem fromPrescriptionItem;

    private String dosage;     
    private String frequency;  
    private String timing;     
    private String notes;      

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private HealthProfile healthProfile;

    @OneToMany(mappedBy = "medicationCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicationReminder> reminders = new ArrayList<>();

    public enum CourseStatus {
        ACTIVE, COMPLETED, STOPPED
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public PrescriptionItem getFromPrescriptionItem() { return fromPrescriptionItem; }
    public void setFromPrescriptionItem(PrescriptionItem fromPrescriptionItem) { this.fromPrescriptionItem = fromPrescriptionItem; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    public HealthProfile getHealthProfile() { return healthProfile; }
    public void setHealthProfile(HealthProfile healthProfile) { this.healthProfile = healthProfile; }

    public List<MedicationReminder> getReminders() { return reminders; }
    public void setReminders(List<MedicationReminder> reminders) { this.reminders = reminders; }
}