package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "insurance_cards")
public class InsuranceCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNumber;       
    private LocalDate issueDate;     
    private LocalDate expiryDate;    
    private String registrationPlace; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private HealthProfile healthProfile;

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getRegistrationPlace() { return registrationPlace; }
    public void setRegistrationPlace(String registrationPlace) { this.registrationPlace = registrationPlace; }

    public HealthProfile getHealthProfile() { return healthProfile; }
    public void setHealthProfile(HealthProfile healthProfile) { this.healthProfile = healthProfile; }
}