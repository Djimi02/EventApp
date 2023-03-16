package com.example.eventappproject.interfaces;

public interface UserDataListener {
    default public void updateUser() {};
    default public void updateCreatedEvents() {};
    default public void updateJoinedEvents() {};
}
