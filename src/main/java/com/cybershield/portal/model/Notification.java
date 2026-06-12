package com.cybershield.portal.model;

public class Notification {

    private Long id;
    private User user;
    private String message;
    private boolean readStatus; // true if read, false if unread
    private String dateCreated; // Stored as formatted String

    public Notification() {}

    public Notification(User user, String message, boolean readStatus, String dateCreated) {
        this.user = user;
        this.message = message;
        this.readStatus = readStatus;
        this.dateCreated = dateCreated;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
