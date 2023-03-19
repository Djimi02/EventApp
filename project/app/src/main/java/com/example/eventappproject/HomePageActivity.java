package com.example.eventappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventappproject.adapters.UserCreatedEventAdapter;
import com.example.eventappproject.adapters.UserJoinedEventAdapter;
import com.example.eventappproject.interfaces.UserCreatedEventRecyclerViewInterface;
import com.example.eventappproject.interfaces.UserDataListener;
import com.example.eventappproject.interfaces.UserJoinedEventRecyclerViewInterface;
import com.example.eventappproject.models.Event;
import com.example.eventappproject.models.User;
import com.example.eventappproject.repositories.UserDataRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity implements UserDataListener, UserCreatedEventRecyclerViewInterface, UserJoinedEventRecyclerViewInterface {

    /* Views */
    private BottomNavigationView bottomNavigationView;
    private Button createEventBTN;
    private RecyclerView createdEventsRV;
    private RecyclerView joinedEventsRV;

    /* Variables */
    private User loggedUser;
    private List<Event> loggedUserCreatedEvents;
    private List<Event> loggedUserJoinedEvents;
    private UserCreatedEventAdapter createdEventAdapter;
    private UserJoinedEventAdapter joinedEventAdapter;

    /* Database */
    private UserDataRepository userDataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        initViews();
        initVars();
    }

    /* This method is responsible for the initialization of all views. */
    private void initViews() {
        this.bottomNavigationView = findViewById(R.id.homePageNavView);
        this.createEventBTN = findViewById(R.id.createEventBTN);
        this.createdEventsRV = findViewById(R.id.createdEventsRecycler);
        this.joinedEventsRV = findViewById(R.id.joinedEventsRecycler);



        // configure navigation bar
        bottomNavigationView.setSelectedItemId(R.id.homeItemNavBar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.mapItemNavBar:
                        Toast.makeText(HomePageActivity.this, "Map selected!", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.homeItemNavBar:
                        return true;
                    case R.id.profileItemNavBar:
                        Intent intent = new Intent(HomePageActivity.this, UserProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    default:
                        return false;
                }
            }
        });

        // set create event btn functionality
        createEventBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateEventDialog();
            }
        });
    }

    /* This method is responsible for the initialization of all variables. */
    private void initVars() {
        this.userDataRepository = UserDataRepository.getInstance();
        userDataRepository.addListener(this);

        this.loggedUser = userDataRepository.getUser();
        this.loggedUserCreatedEvents = userDataRepository.getCreatedEvents();
        this.loggedUserJoinedEvents = userDataRepository.getJoinedEvents();

        this.createdEventAdapter = new UserCreatedEventAdapter(loggedUserCreatedEvents, this);
        this.joinedEventAdapter = new UserJoinedEventAdapter(loggedUserJoinedEvents, this);

        // set adapters to Recycler Views
        createdEventsRV.setAdapter(createdEventAdapter);
        createdEventsRV.setLayoutManager(new LinearLayoutManager(this));
        joinedEventsRV.setAdapter(joinedEventAdapter);
        joinedEventsRV.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * This method is responsible for the logic after Create Event BTN is clicked. This includes
     * creating and showing the dialog window, take the data from the fields then create an event and save
     * this event to the db.
     */
    private void openCreateEventDialog() {

    }

    /**
     * This method is responsible for the logic after click on item in created events recycler
     * occurred. This includes creating and showing the dialog window, display the event's existing
     * data in the fields and when update btn is clicked the method should take the data from the
     * fields and update the event data in the db.
     * @param position - the position of the event in this.loggedUserCreatedEvents
     */
    @Override
    public void onCreatedEventItemClick(int position) {

    }

    /**
     * This method is responsible for the logic after click on item in created events recycler
     * occurred. This includes creating and showing the dialog window, display the event's existing
     * data in the fields.
     * @param position - the position of the event in this.loggedUserJoinedEvents
     */
    @Override
    public void onJoinedEventItemClick(int position) {

    }

    /**
     * This method is called from this.userDataRepository when the data about the created events
     * has changed. Note that this.loggedUserCreatedEvents is a reference to
     * this.userDataRepository.createdEvents, so no other action is needed except to update the
     * recycler view.
     **/
    @Override
    public void updateCreatedEvents() {
        this.createdEventAdapter.notifyDataSetChanged();
    }

    /**
     * This method is called from this.userDataRepository when the data about the joined events
     * has changed. Note that this.loggedUserJoinedEvents is a reference to
     * this.userDataRepository.joinedEvents, so no other action is needed except to update the
     * recycler view.
     **/
    @Override
    public void updateJoinedEvents() {
        this.joinedEventAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationView.setSelectedItemId(R.id.homeItemNavBar);
    }
}