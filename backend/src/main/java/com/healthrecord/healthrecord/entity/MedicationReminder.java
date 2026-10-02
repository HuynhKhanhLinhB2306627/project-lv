package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_reminders")
public class MedicationReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime reminderTime;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isNotified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private MedicationCourse medicationCourse;

    public enum ReminderStatus {
        PENDING, TAKEN, SKIPPED
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }

    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }

    public MedicationCourse getMedicationCourse() { return medicationCourse; }
    public void setMedicationCourse(MedicationCourse medicationCourse) { this.medicationCourse = medicationCourse; }

    public boolean isNotified() { return isNotified; }
    public void setNotified(boolean notified) { isNotified = notified; }
}