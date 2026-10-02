package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {
    List<MedicationReminder> findByReminderTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM MedicationReminder r WHERE r.status = 'PENDING' " +
           "AND r.isNotified = false " +
           "AND r.reminderTime <= :now " +
           "AND r.reminderTime >= :timeLimit")
    List<MedicationReminder> findPendingAndDueReminders(@Param("now") LocalDateTime now, @Param("timeLimit") LocalDateTime timeLimit);
}