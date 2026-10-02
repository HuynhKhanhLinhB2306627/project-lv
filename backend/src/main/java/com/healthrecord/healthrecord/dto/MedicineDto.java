package com.healthrecord.healthrecord.dto;

import jakarta.validation.constraints.NotBlank;

public class MedicineDto {

    private Long id;

    @NotBlank(message = "Tên thuốc không được để trống")
    private String name;

    private String unit;
    private String description;

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}