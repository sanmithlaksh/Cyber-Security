package com.cybershield.portal.model;

public class ThreatIntel {

    private Long id;
    private String name;
    private String category;
    private String riskLevel; // SAFE, MEDIUM, HIGH, CRITICAL
    private String description;
    private String preventionMethods;
    private String dateAdded; // Stored as formatted String

    public ThreatIntel() {}

    public ThreatIntel(String name, String category, String riskLevel, String description, String preventionMethods, String dateAdded) {
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

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}
