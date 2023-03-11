package com.example.eventappproject.models;

import java.time.LocalDateTime;
import java.util.List;

public class Event {
    private String dbID; // reference in the db to this object
    private String name;
    private String description;
    private String location;
    private LocalDateTime date;
    private String creator; // reference in the db to the object
    private List<String> attendees; // reference in the db to the objects

    public Event() {
    }

    public Event(String dbID, String name, String description, String creator) {
        this.dbID = dbID;
        this.name = name;
        this.description = description;
        this.creator = creator;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public void addAttendee(String attendee) {
        this.attendees.add(attendee);
    }
}
