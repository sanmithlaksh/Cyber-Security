package com.cybershield.portal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "threats")
public class ThreatIntel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "threat_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel; // SAFE, MEDIUM, HIGH, CRITICAL

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "prevention_methods", columnDefinition = "TEXT")
    private String preventionMethods;

    @Column(name = "date_added")
    private LocalDate dateAdded;

    public ThreatIntel() {}

    public ThreatIntel(String name, String category, String riskLevel, String description, String preventionMethods, LocalDate dateAdded) {
        this.name = name;
        this.category = category;
        this.riskLevel = riskLevel;
        this.description = description;
        this.preventionMethods = preventionMethods;
        this.dateAdded = dateAdded;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreventionMethods() {
        return preventionMethods;
    }

    public void setPreventionMethods(String preventionMethods) {
        this.preventionMethods = preventionMethods;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }
}
