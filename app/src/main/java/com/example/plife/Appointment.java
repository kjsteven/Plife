package com.example.plife;

import java.io.Serializable;

public class Appointment  implements Serializable {
    private String key; // Unique key for the appointment
    private String title;
    private String description;
    private String date;
    private String time;
    private String userId;
    private String name;
    private String status;
    private String email;

    private static final long serialVersionUID = 1L;

    public Appointment() {
    }

    public Appointment(String key, String title, String description, String date, String time, String userId, String name, String status, String email) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.userId = userId;
        this.name = name;
        this.status = status;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
    public String getKey() {
        return key;
    }
    public String getTitle() {
        return title;
    }
    public String getStatus() {
        return status;
    }
    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
