package com.example.eventappproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eventappproject.adapters.AttendeeAdapter;
import com.example.eventappproject.adapters.UserCreatedEventAdapter;
import com.example.eventappproject.adapters.UserJoinedEventAdapter;
import com.example.eventappproject.interfaces.UserCreatedEventRecyclerViewInterface;
import com.example.eventappproject.interfaces.UserDataListener;
import com.example.eventappproject.interfaces.UserJoinedEventRecyclerViewInterface;
import com.example.eventappproject.models.Event;
import com.example.eventappproject.models.User;
import com.example.eventappproject.repositories.UserDataRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;
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

    /* Dialog */
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    /* Event dialog views */
    private TextView createEventDialogTitle;
    private EditText eventNameCreateEDialog;
    private EditText eventDescCreateEDialog;
    private TextView eventLocCreateEDialog;
    private TextView eventDateCreateEDialog;
    private TextView eventTimeCreateEDialog;
    private EditText eventCapacityCreateEDialog;
    private Button createEventBTNCreateEDialog;
    private Button deleteEventBTNCreateEDialog;
    private Spinner categorySpinner;
    private RecyclerView attendeesRV;
    private CardView attendeeCardView;

    /* Helpers */
    ActivityResultLauncher<Intent> activityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removes title bar from ProfileSettingsActivity
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

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
                        Intent int1 = new Intent(HomePageActivity.this, MapsActivity.class);
                        int1.putExtra("getLocation", "false");
                        startActivity(int1);
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
        // Gets the location from the maps and sets it on the correct field
        this.activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() != 1) {
                            return;
                        }

                        if (result.getData() == null) {
                            return;
                        }

                        LatLng location = (LatLng) result.getData().getExtras().get("location");
                        eventLocCreateEDialog.setText(location.latitude+ "," + location.longitude);
                    }
                });

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
        dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.create_event_dialog, null);

        // init views
        initDialogViews(popupView);
        this.attendeeCardView.setVisibility(View.GONE);

        createEventDialogTitle.setText("Create Event"); // set title of the dialog

        deleteEventBTNCreateEDialog.setVisibility(View.GONE); // hide delete event btn

        configureDateAndTime();

        // Pick location
        eventLocCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationFromMap();;
            }
        });

        // Set create event btn functionality
        createEventBTNCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from fields
                String eventName = eventNameCreateEDialog.getText().toString().trim();
                String eventDesc = eventDescCreateEDialog.getText().toString().trim();
                String eventLoc = eventLocCreateEDialog.getText().toString().trim();
                String eventDate = eventDateCreateEDialog.getText().toString().trim();
                String eventTime = eventTimeCreateEDialog.getText().toString().trim();
                String eventCategory = categorySpinner.getSelectedItem().toString();
                String eventCapacitySTR = eventCapacityCreateEDialog.getText().toString().trim();
                int eventCapacity;
                try {
                    eventCapacity = Integer.parseInt(eventCapacitySTR);
                } catch (Exception e) {
                    eventCapacity = 0;
                }

                if (!dialogDataCheck(eventName, eventLoc, eventDate, eventTime)) {
                    return;
                }

                // Create event and add it to db
                Event newEvent = new Event(eventName, eventDesc, eventLoc, eventDate, eventTime, eventCapacity, eventCategory);
                userDataRepository.addNewEventInDB(newEvent);
                
                dialog.dismiss();
                Toast.makeText(HomePageActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        // show dialog
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
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
        Event selectedEvent = this.loggedUserCreatedEvents.get(position);

        dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.create_event_dialog, null);

        // init views
        initDialogViews(popupView);
        AttendeeAdapter attendeeAdapter = new AttendeeAdapter(selectedEvent, new ArrayList<String>(selectedEvent.getAttendees().keySet()), this);
        this.attendeesRV.setAdapter(attendeeAdapter);
        this.attendeesRV.setLayoutManager(new LinearLayoutManager(this));
;
        createEventDialogTitle.setText("Update Event"); // set title to the dialog

        createEventBTNCreateEDialog.setText("Update event");

        // Set existing event data to the fields in the dialog
        setDataToDialogViews(selectedEvent);

        configureDateAndTime();

        eventLocCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationFromMap();
            }
        });

        // Set the functionality of the update event btn
        createEventBTNCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from fields
                String eventName = eventNameCreateEDialog.getText().toString().trim();
                String eventDesc = eventDescCreateEDialog.getText().toString().trim();
                String eventLoc = eventLocCreateEDialog.getText().toString().trim();
                String eventDate = eventDateCreateEDialog.getText().toString().trim();
                String eventTime = eventTimeCreateEDialog.getText().toString().trim();
                String eventCapacitySTR = eventCapacityCreateEDialog.getText().toString().trim();
                int eventCapacity;
                try {
                    eventCapacity = Integer.parseInt(eventCapacitySTR);
                } catch (Exception e) {
                    eventCapacity = 10;
                }

                if (!dialogDataCheck(eventName, eventLoc, eventDate, eventTime)) {
                    return;
                }

                // Update event and add it to db
                selectedEvent.setName(eventName);
                selectedEvent.setDescription(eventDesc);
                selectedEvent.setLocation(eventLoc);
                selectedEvent.setDate(eventDate);
                selectedEvent.setTime(eventTime);
                selectedEvent.setCapacity(eventCapacity);
                userDataRepository.updateEvent(selectedEvent, selectedEvent.getDbID());

                dialog.dismiss();
                Toast.makeText(HomePageActivity.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the functionality of the delete event btn
        deleteEventBTNCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDataRepository.deleteEvent(selectedEvent);
                
                dialog.dismiss();
                Toast.makeText(HomePageActivity.this, "Event was deleted successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        // show dialog
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private boolean dialogDataCheck(String eventName, String eventLoc, String eventDate, String eventTime) {
        boolean dataCheck = true;
        if (eventName.isEmpty()) {
            eventNameCreateEDialog.setError("Choose a name!");
            dataCheck = false;
        }
        if (eventLoc.isEmpty() || eventLoc.equals("Event Location")) {
            eventLocCreateEDialog.setError("Tap to choose a location!");
            dataCheck = false;
        }
        if (eventDate.isEmpty() || eventDate.equals("Date")) {
            eventDateCreateEDialog.setError("Tap to choose a date!");
            dataCheck = false;
        }
        if (eventTime.isEmpty() || eventTime.equals("Time")) {
            eventTimeCreateEDialog.setError("Tap to choose time!");
            dataCheck = false;
        }

        if (!dataCheck) {
            return false;
        }
        return true;
    }

    /**
     * This method is responsible for the logic after click on item in created events recycler
     * occurred. This includes creating and showing the dialog window, display the event's existing
     * data in the fields.
     * @param position - the position of the event in this.loggedUserJoinedEvents
     */
    @Override
    public void onJoinedEventItemClick(int position) {
        Event selectedEvent = this.loggedUserJoinedEvents.get(position);

        dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.create_event_dialog, null);

        // init views
        initDialogViews(popupView);
        this.attendeeCardView.setVisibility(View.GONE);

        createEventDialogTitle.setText(selectedEvent.getName()); // set title to the dialog

        createEventBTNCreateEDialog.setVisibility(View.GONE);

        eventNameCreateEDialog.setVisibility(View.GONE);

        deleteEventBTNCreateEDialog.setText("Leave event");

        // Make edit text views read-only
        eventNameCreateEDialog.setFocusable(false);
        eventDescCreateEDialog.setFocusable(false);
        eventCapacityCreateEDialog.setFocusable(false);

        // Set existing event data to the fields in the dialog
        setDataToDialogViews(selectedEvent);
        categorySpinner.setEnabled(false);

        // Acts as Leave event btn
        deleteEventBTNCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDataRepository.leaveEvent(selectedEvent);

                Toast.makeText(HomePageActivity.this, "Event left successful!", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        // show dialog
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    @Override
    public void onAttendeeRemoved(Event event, String attendeeID) {
        userDataRepository.kickAttendee(event, attendeeID);
        int currentCap = Integer.parseInt(eventCapacityCreateEDialog.getText().toString().trim());
        currentCap++;
        eventCapacityCreateEDialog.setText(Integer.toString(currentCap));
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

    private void initDialogViews(View popupView) {
        this.createEventDialogTitle = popupView.findViewById(R.id.createEventDialogTitle);
        this.eventNameCreateEDialog = popupView.findViewById(R.id.createEventDialogEventName);
        this.eventDescCreateEDialog = popupView.findViewById(R.id.createEventDialogEventDesc);
        this.eventLocCreateEDialog = popupView.findViewById(R.id.createEventDialogEventLoc);
        this.eventDateCreateEDialog = popupView.findViewById(R.id.createEventDialogEventDate);
        this.eventTimeCreateEDialog = popupView.findViewById(R.id.createEventDialogEventTime);
        this.eventCapacityCreateEDialog = popupView.findViewById(R.id.createEventDialogEventCapacity);
        this.createEventBTNCreateEDialog = popupView.findViewById(R.id.createEventDialogCreateEventBTN);
        this.deleteEventBTNCreateEDialog = popupView.findViewById(R.id.createEventDialogDeleteEventBTN);
        this.categorySpinner = popupView.findViewById(R.id.spinner);
        this.attendeesRV = popupView.findViewById(R.id.attendeesRecycler);
        this.attendeeCardView = popupView.findViewById(R.id.attendeeCardView);

        String[] categories = new String[] {"Party", "Sport", "Culture", "Food", "Drinks", "Other"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(HomePageActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, categories);
        arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        categorySpinner.setAdapter(arrayAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = parent.getItemAtPosition(position).toString();

                categorySpinner.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setDataToDialogViews(Event selectedEvent) {
        eventNameCreateEDialog.setText(selectedEvent.getName());
        eventDescCreateEDialog.setText(selectedEvent.getDescription());
        eventLocCreateEDialog.setText(selectedEvent.getLocation());
        eventDateCreateEDialog.setText(selectedEvent.getDate());
        eventTimeCreateEDialog.setText(selectedEvent.getTime());
        eventCapacityCreateEDialog.setText(Integer.toString(selectedEvent.getCapacity()));
        int index = 0;
        switch (selectedEvent.getCategory()) {
            case "Party":
                index = 0;
                break;
            case "Sport":
                index = 1;
                break;
            case "Culture":
                index = 2;
                break;
            case "Food":
                index = 3;
                break;
            case "Drinks":
                index = 4;
                break;
            case "Other":
                index = 5;
                break;
        }
        categorySpinner.setSelection(index);
    }

    private void configureDateAndTime() {
        // Configure date picker
        eventDateCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        HomePageActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our text view.
                                eventDateCreateEDialog.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        // Configure time picker
        eventTimeCreateEDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our hour and minute.
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // on below line we are creating a variable for time picker dialog.
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        // on below line we are passing context.
                        HomePageActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // on below line we are setting time to our text view.
                                eventTimeCreateEDialog.setText(hourOfDay+ ":" +minute);

                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected time in our time picker.
                        hour, minute, true);
                // at last we are calling show to
                // display our time picker dialog.
                timePickerDialog.show();
            }
        });
    }

    private void getLocationFromMap() {
        Intent int1 = new Intent(HomePageActivity.this, MapsActivity.class);
        int1.putExtra("getLocation", "true");
        activityResultLauncher.launch(int1);
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