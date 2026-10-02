package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.entity.MedicationReminder;
import com.healthrecord.healthrecord.repository.MedicationReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReminderSchedulerService {

    @Autowired
    private MedicationReminderRepository reminderRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 60000) 
    @Transactional
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        
        
        LocalDateTime thirtyMinsFromNow = now.plusMinutes(30);
        LocalDateTime startTime = thirtyMinsFromNow.minusMinutes(5);
        LocalDateTime endTime = thirtyMinsFromNow.plusMinutes(5);
        
        List<MedicationReminder> dueReminders = reminderRepository.findByReminderTimeBetween(startTime, endTime);
        
        for (MedicationReminder reminder : dueReminders) {
            if (reminder.getStatus() == MedicationReminder.ReminderStatus.PENDING && !reminder.isNotified()) {
                try {
                    String email = reminder.getMedicationCourse().getHealthProfile().getUser().getEmail();
                    String profileName = reminder.getMedicationCourse().getHealthProfile().getFullName();
                    String medicineName = reminder.getMedicationCourse().getMedicine().getName();
                    
                    System.out.println("[Scheduler] Gửi nhắc nhở sớm 30p cho: " + email);

                    String dosage = reminder.getMedicationCourse().getDosage();
                    String timing = reminder.getMedicationCourse().getTiming();
                    String timeStr = reminder.getReminderTime().format(DateTimeFormatter.ofPattern("HH:mm"));

                    emailService.sendMedicationReminder(email, profileName, medicineName, dosage, timing, timeStr);
                    
                    reminder.setNotified(true);
                    reminderRepository.save(reminder);
                } catch (Exception e) {
                    System.err.println("[Scheduler] Lỗi gửi mail: " + e.getMessage());
                }
            }
        }
    }
}
