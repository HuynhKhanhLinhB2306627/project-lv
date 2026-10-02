package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medical_facilities")
public class MedicalFacility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    
    @Column(nullable = false)
    private Double latitude;

    
    @Column(nullable = false)
    private Double longitude;

    private String phone;

    @Enumerated(EnumType.STRING)
    private FacilityType type;

    @Transient
    private Double distance;

    public enum FacilityType {
        HOSPITAL, CLINIC, PHARMACY, VACCINATION_CENTER
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public FacilityType getType() { return type; }
    public void setType(FacilityType type) { this.type = type; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
}
