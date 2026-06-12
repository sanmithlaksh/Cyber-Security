package com.cybershield.portal.model;

public class QuizResult {

    private Long id;
    private User user;
    private String category;
    private int score;
    private int totalQuestions;
    private String dateCompleted; // Stored as formatted String
    private String certificateUuid; // UUID for PDF validation

    public QuizResult() {}

    public QuizResult(User user, String category, int score, int totalQuestions, String dateCompleted, String certificateUuid) {
        this.user = user;
        this.category = category;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.dateCompleted = dateCompleted;
        this.certificateUuid = certificateUuid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(String dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public String getCertificateUuid() {
        return certificateUuid;
    }

    public void setCertificateUuid(String certificateUuid) {
        this.certificateUuid = certificateUuid;
    }
}
