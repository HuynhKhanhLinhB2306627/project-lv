package com.healthrecord.healthrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class VaccinationDto {

    private Long id;
    private Long profileId;

    @NotBlank(message = "Tên vắc-xin không được để trống")
    private String vaccineName;

    private Integer doseNumber;

    @NotNull(message = "Ngày tiêm không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vaccinationDate;

    private String location;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }

    public Integer getDoseNumber() { return doseNumber; }
    public void setDoseNumber(Integer doseNumber) { this.doseNumber = doseNumber; }

    public LocalDate getVaccinationDate() { return vaccinationDate; }
    public void setVaccinationDate(LocalDate vaccinationDate) { this.vaccinationDate = vaccinationDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}