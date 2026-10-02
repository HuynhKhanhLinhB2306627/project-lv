package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.VaccinationDto;
import com.healthrecord.healthrecord.entity.Vaccination;
import java.util.List;

public interface VaccinationService {
    
    List<Vaccination> findVaccinationsByProfileId(Long profileId);

    void saveVaccination(VaccinationDto vaccinationDto);
    Vaccination findVaccinationById(Long id);
    void deleteVaccinationById(Long id);

    
    long countVaccinationsByProfileId(Long profileId);
}