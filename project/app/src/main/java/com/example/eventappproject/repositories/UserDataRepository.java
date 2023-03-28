package com.example.eventappproject.repositories;

import androidx.annotation.NonNull;

import com.example.eventappproject.interfaces.UserDataListener;
import com.example.eventappproject.models.Event;
import com.example.eventappproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserDataRepository {

    private String userID;
    private User user;

    private List<Event> createdEvents;

    private List<Event> joinedEvents;

    private List<UserDataListener> listeners;

    private static UserDataRepository instance;

    private DatabaseReference dbReferenceUsers;
    private DatabaseReference dbReferenceEvents;

    private UserDataRepository() {
        createdEvents = new ArrayList<>();
        joinedEvents = new ArrayList<>();
        listeners = new ArrayList<>();

        dbReferenceUsers = FirebaseDatabase.getInstance("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        dbReferenceEvents = FirebaseDatabase.getInstance("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Events");

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // updates the user object
        dbReferenceUsers.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(User.class) != null) {
                    user = snapshot.getValue(User.class);
                    notifyForUserDataChange();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dbReferenceEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dbReferenceUsers.child(userID).child("createdEvents").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        retrieveCreatedEvents(task.getResult());
                    }
                });

                dbReferenceUsers.child(userID).child("joinedEvents").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        retrieveJoinedEvents(task.getResult());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * @return an instance of this class
     */
    public static UserDataRepository getInstance() {
        if (instance == null) {
            instance = new UserDataRepository();
        }
        return instance;
    }

    /**
     * Deletes the existing instance.
     */
    public static void deleteCurrentInstance() {
        instance = null;
    }

    public void addListener(UserDataListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(UserDataListener listener) {
        this.listeners.remove(listener);
    }

    private void notifyForUserDataChange() {
        for (UserDataListener listener : this.listeners) {
            listener.updateUser();
        }
    }

    private void notifyForUserCreatedEventsDataChange() {
        for (UserDataListener listener : this.listeners) {
            listener.updateCreatedEvents();
        }
    }

    private void notifyForUserJoinedEventsDataChange() {
        for (UserDataListener listener : this.listeners) {
            listener.updateJoinedEvents();
        }
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Event> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(List<Event> createdEvents) {
        this.createdEvents = createdEvents;
    }


    public List<Event> getJoinedEvents() {
        return joinedEvents;
    }

    public void setJoinedEvents(List<Event> joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    /**
     * Given DataSnapshot obj of the created events of the logged user, the method retrieves the
     * events as objects and fills them in this.loggedUserCreatedEvents.
     * @param snapshot - data to retrieve from
     * @pre snapshot should be from Users.child(loggedUserID).child("eventsCreated")
     */
    private void retrieveCreatedEvents(DataSnapshot snapshot) {
        System.out.println("CREATED EVENTS DATA CHANGED");
        createdEvents.clear();
        boolean dataFound = false;
        for(DataSnapshot ds : snapshot.getChildren()) {
            dataFound = true;

            String eventID = ds.getValue(String.class);
            dbReferenceEvents.child(eventID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    createdEvents.add(task.getResult().getValue(Event.class));
                    notifyForUserCreatedEventsDataChange();
                }
            });
        }

        if (!dataFound) {
            notifyForUserCreatedEventsDataChange();
        }
    }

    /**
     * Given DataSnapshot obj of the joined events of the logged user, the method retrieves the
     * events as objects and fills them in this.loggedUserJoinedEvents.
     * @param snapshot - data to retrieve from
     * @pre snapshot should be from Users.child(loggedUserID).child("eventsJoined")
     */
    private void retrieveJoinedEvents(DataSnapshot snapshot) {
        System.out.println("JOINED EVENTS DATA CHANGED");
        joinedEvents.clear();
        boolean dataFound = false;
        for(DataSnapshot ds : snapshot.getChildren()) {
            dataFound = true;

            String eventID = ds.getValue(String.class);

            dbReferenceEvents.child(eventID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    joinedEvents.add(task.getResult().getValue(Event.class));
                    notifyForUserJoinedEventsDataChange();
                }
            });
        }

        if (!dataFound) {
            notifyForUserJoinedEventsDataChange();
        }
    }

    /**
     * Adds a new event in the db.
     * @param event - the event data to be added in the db
     */
    public void addNewEventInDB(Event event) {
        String newEventID = this.dbReferenceEvents.push().getKey();
        event.setDbID(newEventID);
        event.setCreator(userID); // setting the logged user as creator
        dbReferenceEvents.child(newEventID).setValue(event);

        user.addCreatedEvent(newEventID);
        dbReferenceUsers.child(userID).setValue(user);
    }

    /**
     * Updates event in the db with new data.
     * @param newEvent - new data to be updated
     * @param eventID - id of the event to be updated
     */
    public void updateEvent(Event newEvent, String eventID) {
        newEvent.setCreator(userID);
        newEvent.setDbID(eventID);
        dbReferenceEvents.child(eventID).setValue(newEvent);
    }

    /**
     * Deletes the event from the db. Deletes the reference of this event from the users that have
     * joined it. Deletes the reference of this event from the creator.
     * @param event - event to be deleted
     */
    public void deleteEvent(Event event) {
        String eventID = event.getDbID();

        // get the most updated info for this event from the db
        dbReferenceEvents.child(eventID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Event event = task.getResult().getValue(Event.class);

                // Remove this event reference from all attendees' joined events lists
                HashMap<String, String> attendees = event.getAttendees();
                if (attendees != null) {
                    for (String userID : attendees.values()) {
                        dbReferenceUsers.child(userID).child("joinedEvents").child(eventID).setValue(null);
                    }
                }

                // Delete the event from db
                dbReferenceEvents.child(eventID).setValue(null);

                // Delete the event from this user's created events list
                user.removeCreatedEvent(eventID);
                dbReferenceUsers.child(userID).setValue(user);
            }
        });
    }

    /**
     * Deletes all created events.
     */
    public void deleteAllEvents() {
        for (Event event : this.createdEvents) {
            deleteEvent(event);
        }
    }

    /**
     * Joins the given event
     * @param event - event to be joined
     */
    public void joinEvent(Event event) {
        // Update event with new attendee
        event.addAttendee(this.userID);
        event.setCapacity(event.getCapacity() - 1); // decrement event capacity
        dbReferenceEvents.child(event.getDbID()).setValue(event);

        // Update this user with new joined event
        user.addJoinedEvent(event.getDbID());
        dbReferenceUsers.child(userID).setValue(user);
    }

    /**
     * Leaves given event.
     * @param event - event to be left
     */
    public void leaveEvent(Event event) {
        // Update event with the removed attendee
        event.removeAttendee(this.userID);
        event.setCapacity(event.getCapacity() + 1);
        dbReferenceEvents.child(event.getDbID()).setValue(event);

        // Update user with removed joined event
        this.user.removeJoinedEvent(event.getDbID());
        dbReferenceUsers.child(this.userID).setValue(user);
    }

    /**
     * Leaves all joined events.
     */
    public void leaveAllEvents() {
        for (Event event : this.joinedEvents) {
            leaveEvent(event);
        }
    }
}