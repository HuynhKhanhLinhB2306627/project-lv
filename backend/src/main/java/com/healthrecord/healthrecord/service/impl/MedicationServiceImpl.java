package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.MedicationCourseDto;
import com.healthrecord.healthrecord.entity.*;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.repository.MedicationCourseRepository;
import com.healthrecord.healthrecord.repository.MedicationReminderRepository;
import com.healthrecord.healthrecord.repository.MedicineRepository;
import com.healthrecord.healthrecord.service.MedicationService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MedicationServiceImpl implements MedicationService {

    @Autowired private MedicationCourseRepository courseRepository;
    @Autowired private MedicationReminderRepository reminderRepository;
    @Autowired private HealthProfileRepository healthProfileRepository;
    @Autowired private MedicineRepository medicineRepository;
    @Autowired private ProfileAccessService profileAccessService;

    
    private HealthProfile validateProfileAccess(Long profileId, boolean requireEditorRole) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || profileId == null) return null;

        HealthProfile profile = healthProfileRepository.findById(profileId).orElse(null);
        if (profile == null) return null;

        boolean isOwner = profile.getUser().getId().equals(currentUser.getId());

        
        if (isOwner) return profile;

        
        if (requireEditorRole) {
            boolean isEditor = profileAccessService.hasEditorAccess(currentUser.getId(), profileId);
            if (!isEditor) {
                throw new SecurityException("Bạn không có quyền chỉnh sửa hồ sơ này.");
            }
        } else {
            
            boolean hasAccess = profileAccessService.getAccessListByProfileId(profileId).stream()
                    .anyMatch(access -> access.getUser().getId().equals(currentUser.getId()));
            if (!hasAccess) return null;
        }

        return profile;
    }

    @Override
    @Transactional
    public void saveMedicationCourse(MedicationCourseDto courseDto) {
        
        HealthProfile profile = validateProfileAccess(courseDto.getProfileId(), true);
        if (profile == null) {
            throw new IllegalArgumentException("Hồ sơ không tồn tại hoặc không có quyền truy cập");
        }

        Medicine medicine = medicineRepository.findById(courseDto.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("Thuốc không tồn tại"));

        MedicationCourse course = new MedicationCourse();
        course.setHealthProfile(profile);
        course.setMedicine(medicine);
        course.setDosage(courseDto.getDosage());
        course.setFrequency(courseDto.getFrequency());
        course.setTiming(courseDto.getTiming());
        course.setNotes(courseDto.getNotes());
        course.setStartDate(courseDto.getStartDate());
        course.setEndDate(courseDto.getStartDate().plusDays(courseDto.getDurationInDays() - 1));
        course.setStatus(MedicationCourse.CourseStatus.ACTIVE);

        MedicationCourse savedCourse = courseRepository.save(course);

        
        List<MedicationReminder> reminders = generateRemindersForCourse(savedCourse, courseDto.getReminderTimes());
        reminderRepository.saveAll(reminders);
    }

    @Override
    public List<MedicationCourse> findAllCoursesByProfileId(Long profileId) {
        
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return courseRepository.findByHealthProfileOrderByStartDateDesc(profile);
        }
        return Collections.emptyList();
    }

    @Override
    public List<MedicationReminder> findTodayRemindersByCurrentUser() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return Collections.emptyList();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        
        List<HealthProfile> accessibleProfiles = healthProfileRepository.findAll().stream()
                .filter(p -> {
                    boolean isOwner = p.getUser().getId().equals(currentUser.getId());
                    boolean hasAccess = profileAccessService.getAccessListByProfileId(p.getId()).stream()
                            .anyMatch(access -> access.getUser().getId().equals(currentUser.getId()));
                    return isOwner || hasAccess;
                })
                .toList();

        List<MedicationReminder> allReminders = new ArrayList<>();

        for (HealthProfile profile : accessibleProfiles) {
            for (MedicationCourse course : profile.getMedicationCourses()) {
                if (course.getStatus() == MedicationCourse.CourseStatus.ACTIVE) {
                    for (MedicationReminder reminder : course.getReminders()) {
                        if (!reminder.getReminderTime().isBefore(startOfDay) && !reminder.getReminderTime().isAfter(endOfDay)) {
                            allReminders.add(reminder);
                        }
                    }
                }
            }
        }
        
        
        allReminders.sort((r1, r2) -> r1.getReminderTime().compareTo(r2.getReminderTime()));
        
        return allReminders;
    }

    @Override
    public MedicationCourse findCourseById(Long id) {
        MedicationCourse course = courseRepository.findById(id).orElse(null);
        if (course == null) return null;

        
        HealthProfile profile = validateProfileAccess(course.getHealthProfile().getId(), false);
        return (profile != null) ? course : null;
    }

    @Override
    @Transactional
    public void updateMedicationCourse(Long id, MedicationCourseDto courseDto) {
        MedicationCourse course = courseRepository.findById(id).orElse(null);
        if (course == null) return;

        
        validateProfileAccess(course.getHealthProfile().getId(), true);

        
        if (!course.getMedicine().getId().equals(courseDto.getMedicineId())) {
            Medicine medicine = medicineRepository.findById(courseDto.getMedicineId())
                    .orElseThrow(() -> new IllegalArgumentException("Thuốc không tồn tại"));
            course.setMedicine(medicine);
        }

        course.setDosage(courseDto.getDosage());
        course.setFrequency(courseDto.getFrequency());
        course.setTiming(courseDto.getTiming());
        course.setNotes(courseDto.getNotes());
        course.setStartDate(courseDto.getStartDate());
        course.setEndDate(courseDto.getStartDate().plusDays(courseDto.getDurationInDays() - 1));

        
        course.getReminders().clear();
        List<MedicationReminder> newReminders = generateRemindersForCourse(course, courseDto.getReminderTimes());
        course.getReminders().addAll(newReminders);

        courseRepository.save(course);
    }

    @Override
    public void deleteCourseById(Long id) {
        MedicationCourse course = courseRepository.findById(id).orElse(null);
        if (course != null) {
            
            validateProfileAccess(course.getHealthProfile().getId(), true);
            courseRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void updateReminderStatus(Long reminderId, MedicationReminder.ReminderStatus status) {
        MedicationReminder reminder = reminderRepository.findById(reminderId).orElse(null);
        if (reminder != null) {
            
            validateProfileAccess(reminder.getMedicationCourse().getHealthProfile().getId(), true);
            reminder.setStatus(status);
            reminderRepository.save(reminder);
        }
    }

    @Override
    public long countCoursesByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return courseRepository.countByHealthProfile(profile);
        }
        return 0;
    }

    
    private List<MedicationReminder> generateRemindersForCourse(MedicationCourse course, List<LocalTime> times) {
        List<MedicationReminder> reminders = new ArrayList<>();
        LocalDate currentDate = course.getStartDate();

        
        if (times == null || times.isEmpty()) {
            times = new ArrayList<>();
            if ("1 lần/ngày".equalsIgnoreCase(course.getFrequency())) {
                times.add(LocalTime.of(8, 0));
            } else if ("2 lần/ngày".equalsIgnoreCase(course.getFrequency())) {
                times.add(LocalTime.of(8, 0));
                times.add(LocalTime.of(20, 0));
            } else {
                times.add(LocalTime.of(8, 0));
                times.add(LocalTime.of(12, 0));
                times.add(LocalTime.of(20, 0));
            }
        }

        while (!currentDate.isAfter(course.getEndDate())) {
            for (LocalTime time : times) {
                MedicationReminder reminder = new MedicationReminder();
                reminder.setMedicationCourse(course);
                reminder.setReminderTime(LocalDateTime.of(currentDate, time));
                reminder.setStatus(MedicationReminder.ReminderStatus.PENDING);
                reminders.add(reminder);
            }
            currentDate = currentDate.plusDays(1);
        }
        return reminders;
    }
}
