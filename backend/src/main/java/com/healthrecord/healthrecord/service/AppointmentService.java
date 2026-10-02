package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.AppointmentDto;
import com.healthrecord.healthrecord.entity.Appointment;
import java.util.List;
import java.util.Map;

public interface AppointmentService {

    List<Appointment> findAppointmentsByProfileId(Long profileId);

    void saveAppointment(AppointmentDto appointmentDto);

    Appointment findAppointmentById(Long id);

    void deleteAppointmentById(Long id);

    long countAppointmentsByProfileId(Long profileId);

    Map<String, Long> getMonthlyAppointmentCounts(Long profileId);
}