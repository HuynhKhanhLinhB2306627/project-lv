package com.healthrecord.healthrecord.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class MedicationCourseDto {
    private Long id;
    private Long profileId;

    
    private Long medicineId;

    private String dosage;
    private String frequency;
    private String timing;
    private String notes;

    private java.util.List<java.time.LocalTime> reminderTimes;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    private int durationInDays;

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public java.util.List<java.time.LocalTime> getReminderTimes() { return reminderTimes; }
    public void setReminderTimes(java.util.List<java.time.LocalTime> reminderTimes) { this.reminderTimes = reminderTimes; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public int getDurationInDays() { return durationInDays; }
    public void setDurationInDays(int durationInDays) { this.durationInDays = durationInDays; }
}