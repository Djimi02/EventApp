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

        // updates created events data
        dbReferenceUsers.child(userID).child("createdEvents").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                createdEvents.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String eventID = ds.getKey();

                    // find and retrieves the event as object
                    dbReferenceEvents.child(eventID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            createdEvents.add(task.getResult().getValue(Event.class));
                        }
                    });
                }
                notifyForUserCreatedEventsDataChange();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // updates joined events data
        dbReferenceUsers.child(userID).child("joinedEvents").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                joinedEvents.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String eventID = ds.getKey();

                    // find and retrieves the event as object
                    dbReferenceEvents.child(eventID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            joinedEvents.add(task.getResult().getValue(Event.class));
                        }
                    });
                }
                notifyForUserJoinedEventsDataChange();
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
     * Adds a new event in the db.
     * @param event - the event data to be added in the db
     */
    public void addNewEventInDB(Event event) {
        String newEventID = this.dbReferenceEvents.push().getKey();
        event.setDbID(newEventID);
        dbReferenceEvents.child(newEventID).setValue(event);
    }

    /**
     * Updates event in the db with new data.
     * @param newEvent - new data to be updated
     * @param eventID - id of the event to be updated
     */
    public void updateEvent(Event newEvent, String eventID) {
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

                HashMap<String, String> attendees = event.getAttendees();
                if (attendees != null) {
                    for (String userID : attendees.values()) {
                        dbReferenceUsers.child(userID).child("joinedEvents").child(eventID).setValue(null);
                    }
                }

                dbReferenceEvents.child(eventID).setValue(null);

                user.removeCreatedEvent(eventID);
                dbReferenceUsers.child(userID).setValue(user);
            }
        });
    }
}