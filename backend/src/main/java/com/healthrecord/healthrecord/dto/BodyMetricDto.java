package com.healthrecord.healthrecord.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;










public class BodyMetricDto {

    private Long id;
    private Long profileId;

    
    @NotNull(message = "Chiều cao không được để trống")
    @Min(value = 50, message = "Chiều cao phải từ 50 đến 250 cm")
    @Max(value = 250, message = "Chiều cao phải từ 50 đến 250 cm")
    private Double heightInCm;

    
    @NotNull(message = "Cân nặng không được để trống")
    @Min(value = 10, message = "Cân nặng phải từ 10 đến 400 kg")
    @Max(value = 400, message = "Cân nặng phải từ 10 đến 400 kg")
    private Double weightInKg;

    
    @Min(value = 60, message = "Huyết áp tâm thu phải từ 60 đến 300 mmHg")
    @Max(value = 300, message = "Huyết áp tâm thu phải từ 60 đến 300 mmHg")
    private Integer systolicBp;

    
    @Min(value = 30, message = "Huyết áp tâm trương phải từ 30 đến 200 mmHg")
    @Max(value = 200, message = "Huyết áp tâm trương phải từ 30 đến 200 mmHg")
    private Integer diastolicBp;

    
    
    @Min(value = 1, message = "Chỉ số mỡ máu chỉ hợp lệ với các mức 1, 2 hoặc 3")
    @Max(value = 3, message = "Chỉ số mỡ máu chỉ hợp lệ với các mức 1, 2 hoặc 3")
    private Integer cholesterol;

    
    
    @Min(value = 1, message = "Chỉ số đường huyết chỉ hợp lệ với các mức 1, 2 hoặc 3")
    @Max(value = 3, message = "Chỉ số đường huyết chỉ hợp lệ với các mức 1, 2 hoặc 3")
    private Integer glucose;

    private Boolean smoking;
    private Boolean alcohol;
    private Boolean physicalActivity;

    private java.time.LocalDateTime logDate;

    
    @AssertTrue(message = "Phải nhập đồng thời cả huyết áp tâm thu và huyết áp tâm trương")
    public boolean isBloodPressurePairValid() {
        return (systolicBp == null && diastolicBp == null)
                || (systolicBp != null && diastolicBp != null);
    }

    
    @AssertTrue(message = "Huyết áp tâm thu phải lớn hơn huyết áp tâm trương")
    public boolean isBloodPressureLogical() {
        if (systolicBp == null || diastolicBp == null) {
            return true;
        }
        return systolicBp > diastolicBp;
    }

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Double getHeightInCm() {
        return heightInCm;
    }

    public void setHeightInCm(Double heightInCm) {
        this.heightInCm = heightInCm;
    }

    public Double getWeightInKg() {
        return weightInKg;
    }

    public void setWeightInKg(Double weightInKg) {
        this.weightInKg = weightInKg;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Integer getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(Integer cholesterol) {
        this.cholesterol = cholesterol;
    }

    public Integer getGlucose() {
        return glucose;
    }

    public void setGlucose(Integer glucose) {
        this.glucose = glucose;
    }

    public Boolean getSmoking() {
        return smoking;
    }

    public void setSmoking(Boolean smoking) {
        this.smoking = smoking;
    }

    public Boolean getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(Boolean alcohol) {
        this.alcohol = alcohol;
    }

    public Boolean getPhysicalActivity() {
        return physicalActivity;
    }

    public void setPhysicalActivity(Boolean physicalActivity) {
        this.physicalActivity = physicalActivity;
    }

    public java.time.LocalDateTime getLogDate() {
        return logDate;
    }

    public void setLogDate(java.time.LocalDateTime logDate) {
        this.logDate = logDate;
    }
}