package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.MedicationCourseDto;
import com.healthrecord.healthrecord.entity.MedicationCourse;
import com.healthrecord.healthrecord.entity.MedicationReminder;
import java.util.List;

public interface MedicationService {

    void saveMedicationCourse(MedicationCourseDto courseDto);

    List<MedicationCourse> findAllCoursesByProfileId(Long profileId);

    
    List<MedicationReminder> findTodayRemindersByCurrentUser();

    MedicationCourse findCourseById(Long id);

    void updateMedicationCourse(Long id, MedicationCourseDto courseDto);

    void deleteCourseById(Long id);

    void updateReminderStatus(Long reminderId, MedicationReminder.ReminderStatus status);

    long countCoursesByProfileId(Long profileId);
}