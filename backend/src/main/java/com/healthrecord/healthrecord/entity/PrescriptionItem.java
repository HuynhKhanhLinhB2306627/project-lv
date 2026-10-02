package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    private Integer quantity; 

    private String dosage; 

    private Integer durationInDays; 

    private String morningDose;   
    private String afternoonDose; 
    private String eveningDose;   
    private String noonDose;      

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public Integer getDurationInDays() { return durationInDays; }
    public void setDurationInDays(Integer durationInDays) { this.durationInDays = durationInDays; }
    public String getMorningDose() { return morningDose; }
    public void setMorningDose(String morningDose) { this.morningDose = morningDose; }
    public String getAfternoonDose() { return afternoonDose; }
    public void setAfternoonDose(String afternoonDose) { this.afternoonDose = afternoonDose; }
    public String getEveningDose() { return eveningDose; }
    public void setEveningDose(String eveningDose) { this.eveningDose = eveningDose; }
    public String getNoonDose() { return noonDose; }
    public void setNoonDose(String noonDose) { this.noonDose = noonDose; }
}