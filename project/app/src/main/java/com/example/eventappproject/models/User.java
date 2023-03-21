package com.example.eventappproject.models;

import java.util.HashMap;
import java.util.List;

public class User {
    private String dbID; // reference in the db to this object
    private String email;
    private String name;
    private String description;
    private HashMap<String, String> createdEvents; // reference in the db to the objects
    private HashMap<String, String> joinedEvents; // reference in the db to the objects

    public User() {
        this.joinedEvents = new HashMap<>();
        this.createdEvents = new HashMap<>();
    }

    public User(String dbID, String email, String name) {
        this.dbID = dbID;
        this.email = email;
        this.name = name;
        this.joinedEvents = new HashMap<>();
        this.createdEvents = new HashMap<>();
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return this.name;
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

    public HashMap<String, String> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(HashMap<String, String> createdEvents) {
        this.createdEvents = createdEvents;
    }

    public HashMap<String, String> getJoinedEvents() {
        return joinedEvents;
    }

    public void setJoinedEvents(HashMap<String, String> joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    public void addCreatedEvent(String eventID) {
        this.createdEvents.put(eventID, eventID);
    }

    public void addJoinedEvent(String eventID) {
        this.joinedEvents.put(eventID, eventID);
    }

    public void removeCreatedEvent(String eventID) {this.createdEvents.remove(eventID); }

    public void removeJoinedEvent(String eventID) {
        this.joinedEvents.remove(eventID);
    }
}
