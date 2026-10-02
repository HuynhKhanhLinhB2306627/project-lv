package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.Appointment;
import com.healthrecord.healthrecord.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    
    List<Appointment> findByHealthProfileOrderByAppointmentDateDesc(HealthProfile healthProfile);

    
    List<Appointment> findByHealthProfileAndAppointmentDateAfterOrderByAppointmentDateAsc(HealthProfile healthProfile, LocalDateTime currentDate);

    
    long countByHealthProfile(HealthProfile healthProfile);

    
    @Query("SELECT FUNCTION('TO_CHAR', a.appointmentDate, 'YYYY-MM'), COUNT(a) " +
            "FROM Appointment a WHERE a.healthProfile = :profile " +
            "GROUP BY FUNCTION('TO_CHAR', a.appointmentDate, 'YYYY-MM') " +
            "ORDER BY FUNCTION('TO_CHAR', a.appointmentDate, 'YYYY-MM') ASC")
    List<Object[]> countAppointmentsByMonth(@Param("profile") HealthProfile profile);
}