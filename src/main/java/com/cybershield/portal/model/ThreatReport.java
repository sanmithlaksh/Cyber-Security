package com.cybershield.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class ThreatReport {

    @Id
    @Column(name = "report_id")
    private String reportId; // Format: CYB-YYYY-NNNN

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user; // null if reported anonymously

    @Column(nullable = false)
    private String title;

    @Column(name = "threat_type", nullable = false)
    private String threatType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "evidence_path")
    private String evidencePath;

    private String location;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "risk_score")
    private int riskScore;

    private String severity; // SAFE, MEDIUM, HIGH, CRITICAL

    private String status; // SUBMITTED, UNDER_REVIEW, INVESTIGATING, RESOLVED, CLOSED

    @Column(name = "analyst_notes", columnDefinition = "TEXT")
    private String analystNotes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "analyst_id")
    private User analyst; // Analyst investigating the case

    public ThreatReport() {}

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThreatType() {
        return threatType;
    }

    public void setThreatType(String threatType) {
        this.threatType = threatType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(String evidencePath) {
        this.evidencePath = evidencePath;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAnalystNotes() {
        return analystNotes;
    }

    public void setAnalystNotes(String analystNotes) {
        this.analystNotes = analystNotes;
    }

    public User getAnalyst() {
        return analyst;
    }

    public void setAnalyst(User analyst) {
        this.analyst = analyst;
    }
}
