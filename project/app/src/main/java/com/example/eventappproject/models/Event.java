package com.example.eventappproject.models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Event {
    private String dbID; // reference in the db to this object
    private String name;
    private String description;
    private String location;
    private String date;
    private String time;
    private String creator; // reference in the db to the object
    private HashMap<String, String> attendees; // reference in the db to the objects
    private int capacity;

    public Event() {
        this.attendees = new HashMap<>();
    }

    public Event(String name, String description, String location, String date, String time, int capacity) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.capacity = capacity;
        this.attendees = new HashMap<>();
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public HashMap<String, String> getAttendees() {
        return attendees;
    }

    public void setAttendees(HashMap<String, String> attendees) {
        this.attendees = attendees;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void addAttendee(String attendeeID) {
        this.attendees.put(attendeeID, attendeeID);
    }
}
