package com.example.eventappproject.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventappproject.R;
import com.example.eventappproject.adapters.UserJoinedEventAdapter;
import com.example.eventappproject.interfaces.UserJoinedEventRecyclerViewInterface;
import com.example.eventappproject.models.Event;
import com.example.eventappproject.repositories.UserDataRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AllEventsFragment extends SupportMapFragment implements OnMapReadyCallback, UserJoinedEventRecyclerViewInterface {

    /* Views */
    private ImageButton searchBTN;
    private EditText searchEventET;
    private NavigationView filterMenu;
    private CheckBox partyCheckBox;
    private CheckBox sportCheckBox;
    private CheckBox cultureCheckBox;
    private CheckBox foodCheckBox;
    private CheckBox drinksCheckBox;
    private CheckBox otherCheckBox;
    private RecyclerView eventListRV;

    /* Attributes */
    Context parent;
    List<Event> allEvents;
    Map<Marker, Event> eventMarkerMap;

    /* Helpers */
    UserJoinedEventAdapter allEventsAdapter;

    /* Database */
    DatabaseReference events;
    UserDataRepository userDataRepository;

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
    private Button joinEventBTN;
    private Button deleteEventBTN;
    private Spinner categorySpinner;
    private CardView attendeeCardView;

    /* Maps */
    GoogleMap mMap;
    private static int LOCATION_PERMISSION_CODE = 101;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public AllEventsFragment(Context parent) {
        this.parent = parent;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(parent);

        initVars();

        events = FirebaseDatabase.getInstance("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Events");
        initViews();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                if (eventMarkerMap.get(marker) != null) {
                    viewEvent(eventMarkerMap.get(marker));
                }

                return false;
            }
        });

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        events.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                allEvents.clear();
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    Event event = ds.getValue(Event.class);

                    if (!isEventEligibleToShow(event)) {
                        continue;
                    }

                    allEvents.add(event);
                }
                allEventsAdapter.notifyDataSetChanged();

                showEventsOnMap(allEvents);
            }
        });


        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void initVars() {
        this.allEvents = new ArrayList<>();
        this.eventMarkerMap = new HashMap<>();
        this.allEventsAdapter = new UserJoinedEventAdapter(allEvents, this);
        this.userDataRepository = UserDataRepository.getInstance();
    }

    private void initViews() {
        // Listener to filter data on checkbox checked change
        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterEventData();
            }
        };

        this.searchBTN = ((Activity)parent).findViewById(R.id.searchButton);
        this.searchEventET = (EditText)((Activity)parent).findViewById(R.id.searchET);
        this.filterMenu = ((Activity)parent).findViewById(R.id.navView);

        searchEventET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEventET.setCursorVisible(true);
            }
        });

        this.partyCheckBox = (CheckBox) filterMenu.getMenu().getItem(0).getSubMenu().getItem(0).getActionView();
        partyCheckBox.setChecked(true);
        partyCheckBox.setOnCheckedChangeListener(checkedChangeListener);

        this.sportCheckBox = (CheckBox) filterMenu.getMenu().getItem(0).getSubMenu().getItem(1).getActionView();
        sportCheckBox.setChecked(true);
        sportCheckBox.setOnCheckedChangeListener(checkedChangeListener);

        this.cultureCheckBox = (CheckBox) filterMenu.getMenu().getItem(0).getSubMenu().getItem(2).getActionView();
        cultureCheckBox.setChecked(true);
        cultureCheckBox.setOnCheckedChangeListener(checkedChangeListener);

        this.foodCheckBox = (CheckBox) filterMenu.getMenu().getItem(0).getSubMenu().getItem(3).getActionView();
        foodCheckBox.setChecked(true);
        foodCheckBox.setOnCheckedChangeListener(checkedChangeListener);

        this.drinksCheckBox = (CheckBox) filterMenu.getMenu().getItem(0).getSubMenu().getItem(4).getActionView();
        drinksCheckBox.setChecked(true);
        drinksCheckBox.setOnCheckedChangeListener(checkedChangeListener);

        this.otherCheckBox = (CheckBox) filterMenu.getMenu().getItem(0).getSubMenu().getItem(5).getActionView();
        otherCheckBox.setChecked(true);
        otherCheckBox.setOnCheckedChangeListener(checkedChangeListener);


        this.eventListRV = (RecyclerView) filterMenu.getMenu().findItem(R.id.eventsList).getActionView().findViewById(R.id.allEventsMapRV);
        eventListRV.setAdapter(allEventsAdapter);
        eventListRV.setLayoutManager(new LinearLayoutManager(parent));
        allEventsAdapter.notifyDataSetChanged();

        searchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterEventData();
                hideSoftKeyboard(((Activity) parent));
            }
        });
    }

    /**
     * Visualizes on the map, the events that correspond to the filtered data selected by the user.
     * This includes text in the search bar, selected categories and the time period.
     */
    private void filterEventData() {
        String searchText = searchEventET.getText().toString().trim();

        List<String> selectedCategories = getSelectedCategories();

        List<Event> newCreatedEventsList;

        System.out.println("All");
        newCreatedEventsList = allEvents.stream()
                .filter(event -> event.getName().toLowerCase().contains(searchText.toLowerCase()) && selectedCategories.contains(event.getCategory()))
                .collect(Collectors.toList());

        mMap.clear();
        showEventsOnMap(newCreatedEventsList); // Show eventS on map
        allEventsAdapter.setEvents(newCreatedEventsList); // Update the list of events
        allEventsAdapter.notifyDataSetChanged();
    }

    /**
     * Returns a list containing all selected categories.
     * @return list containing all the selected categories in the form of Strings
     */
    private List<String> getSelectedCategories() {
        List<String> selectedCategories = new ArrayList<>();

        if (partyCheckBox.isChecked()) {
            selectedCategories.add("Party");
        }

        if (sportCheckBox.isChecked()) {
            selectedCategories.add("Sport");
        }

        if (cultureCheckBox.isChecked()) {
            selectedCategories.add("Culture");
        }

        if (foodCheckBox.isChecked()) {
            selectedCategories.add("Food");
        }

        if (drinksCheckBox.isChecked()) {
            selectedCategories.add("Drinks");
        }

        if (otherCheckBox.isChecked()) {
            selectedCategories.add("Other");
        }

        return selectedCategories;
    }

    private void showEventsOnMap(List<Event> events) {

        for (Event event : events) {
            String[] latLng = event.getLocation().split(",");
            double lat = Double.parseDouble(latLng[0]);
            double lng = Double.parseDouble(latLng[1]);

            Marker pin = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(event.getName()));
            eventMarkerMap.put(pin, event);
        }
    }

    private boolean isEventEligibleToShow(Event event) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.ENGLISH);
        boolean output = false;
        try {
            Date dateTime = formatter.parse(event.getDate() + " " + event.getTime());
            if (dateTime.after(new Date()) && event.getCapacity() > 0) {
                output = true;
            }
        } catch (Exception e) {}
        return output;
    }

    @Override
    public void onJoinedEventItemClick(int position) {
        viewEvent(allEvents.get(position));
    }

    private void viewEvent(Event event) {
        Event selectedEvent = event;

        dialogBuilder = new AlertDialog.Builder(parent);
        final View popupView = getLayoutInflater().inflate(R.layout.create_event_dialog, null);

        // init views
        initDialogViews(popupView);

        createEventDialogTitle.setText(selectedEvent.getName()); // set title to the dialog

        deleteEventBTN.setVisibility(View.GONE);

        eventNameCreateEDialog.setVisibility(View.GONE);

        joinEventBTN.setText("Join");
//        joinEventBTN.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC);
        if (event.getCreator().equals(userDataRepository.getUserID())) {
            joinEventBTN.setText("Created");
            joinEventBTN.setEnabled(false);
        } else {
            for (String attendees : event.getAttendees().values()) {
                if (attendees.equals(userDataRepository.getUserID())) {
                    joinEventBTN.setText("Joined");
                    joinEventBTN.setEnabled(false);
                }
            }
        }

//        joinEventBTN.setBackgroundColor(getResources().getColor(R.color.green));

        // Make edit text views read-only
        eventNameCreateEDialog.setFocusable(false);
        eventDescCreateEDialog.setFocusable(false);
        eventCapacityCreateEDialog.setFocusable(false);

        // Set existing event data to the fields in the dialog
        setDataToDialogViews(selectedEvent);

        // Acts as Leave event btn
        joinEventBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDataRepository.joinEvent(selectedEvent);

                Toast.makeText(parent, "Event joined successfully!", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        // show dialog
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void initDialogViews(View popupView) {
        this.createEventDialogTitle = popupView.findViewById(R.id.createEventDialogTitle);
        this.eventNameCreateEDialog = popupView.findViewById(R.id.createEventDialogEventName);
        this.eventDescCreateEDialog = popupView.findViewById(R.id.createEventDialogEventDesc);
        this.eventLocCreateEDialog = popupView.findViewById(R.id.createEventDialogEventLoc);
        this.eventDateCreateEDialog = popupView.findViewById(R.id.createEventDialogEventDate);
        this.eventTimeCreateEDialog = popupView.findViewById(R.id.createEventDialogEventTime);
        this.eventCapacityCreateEDialog = popupView.findViewById(R.id.createEventDialogEventCapacity);
        this.joinEventBTN = popupView.findViewById(R.id.createEventDialogCreateEventBTN);
        this.deleteEventBTN = popupView.findViewById(R.id.createEventDialogDeleteEventBTN);
        this.categorySpinner = popupView.findViewById(R.id.spinner);
        this.attendeeCardView = popupView.findViewById(R.id.attendeeCardView);
        this.attendeeCardView.setVisibility(View.GONE);

        String[] categories = new String[] {"Party", "Sport", "Culture", "Food", "Drinks", "Other"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(parent, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, categories);
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
        categorySpinner.setEnabled(false);
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (isLocationPermissionGranted()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                requestLocationPermission();
            }
        } catch (SecurityException e)  {
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (isLocationPermissionGranted()) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener((Activity) parent, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), 15f));
                            }
                        } else {
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(parent, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions((Activity) parent, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }

    private void hideSoftKeyboard(Activity activity) {
        searchEventET.setCursorVisible(false);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}