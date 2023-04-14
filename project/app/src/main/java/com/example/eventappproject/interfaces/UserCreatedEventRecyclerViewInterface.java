package com.example.eventappproject.interfaces;

import com.example.eventappproject.models.Event;

public interface UserCreatedEventRecyclerViewInterface {
    public void onCreatedEventItemClick(int position);
    public void onAttendeeRemoved(Event event, String attendeeID);
}
