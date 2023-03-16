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
import java.util.List;

public class UserDataRepository {

    private String userID;
    private User user;

    private List<Event> createdEvents;
    private List<String> createdEventsIDs;

    private List<Event> joinedEvents;
    private List<String> joinedEventsIDs;

    private List<UserDataListener> listeners;

    private static UserDataRepository instance;

    private DatabaseReference dbReferenceUsers;
    private DatabaseReference dbReferenceEvents;

    private UserDataRepository() {
        createdEvents = new ArrayList<>();
        createdEventsIDs = new ArrayList<>();
        joinedEvents = new ArrayList<>();
        joinedEventsIDs = new ArrayList<>();
        listeners = new ArrayList<>();

        dbReferenceUsers = FirebaseDatabase.getInstance().getReference("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").child("Users");
        dbReferenceEvents = FirebaseDatabase.getInstance().getReference("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").child("Events");

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
                createdEventsIDs.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String eventID = ds.getKey();
                    createdEventsIDs.add(eventID);

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
                joinedEventsIDs.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String eventID = ds.getKey();
                    joinedEventsIDs.add(eventID);

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
        })
    }

    public static UserDataRepository getInstance() {
        if (instance == null) {
            instance = new UserDataRepository();
        }
        return instance;
    }

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

    public List<String> getCreatedEventsIDs() {
        return createdEventsIDs;
    }

    public void setCreatedEventsIDs(List<String> createdEventsIDs) {
        this.createdEventsIDs = createdEventsIDs;
    }

    public List<Event> getJoinedEvents() {
        return joinedEvents;
    }

    public void setJoinedEvents(List<Event> joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    public List<String> getJoinedEventsIDs() {
        return joinedEventsIDs;
    }

    public void setJoinedEventsIDs(List<String> joinedEventsIDs) {
        this.joinedEventsIDs = joinedEventsIDs;
    }
}