package com.example.eventappproject.models;

import java.util.List;

public class User {
    private String dbID; // reference in the db to this object
    private String email;
    private String password;
    private String name;
    private String description;
    private List<String> createdEvents; // reference in the db to the objects
    private List<String> joinedEvents; // reference in the db to the objects

    public User() {
    }

    public User(String dbID, String email, String password, String name) {
        this.dbID = dbID;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(List<String> createdEvents) {
        this.createdEvents = createdEvents;
    }

    public List<String> getJoinedEvents() {
        return joinedEvents;
    }

    public void setJoinedEvents(List<String> joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    public void addCreatedEvent(String event) {
        this.createdEvents.add(event);
    }

    public void addJoinedEvents(String event) {

    }
}
