package com.example.eventappproject;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.eventappproject.fragments.AllEventsFragment;
import com.example.eventappproject.fragments.PickLocationFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.eventappproject.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MapsActivity extends FragmentActivity {

    /* Views */
    private BottomNavigationView bottomNavigationView;
    private ImageButton menuButton;
    private DrawerLayout mDrawerLayout;
    private CardView searchBar;

    /* Maps */
    private ActivityMapsBinding binding;
    private static int LOCATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Ask for location permission
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
        }

        initViews();

        if (getIntent().getExtras().get("getLocation").equals("true")){
            bottomNavigationView.setVisibility(View.GONE);
            searchBar.setVisibility(View.GONE);
            PickLocationFragment fragment = new PickLocationFragment(this);
            mapFragment.getMapAsync(fragment);
            return;
        } else {
            AllEventsFragment fragment = new AllEventsFragment(this);
            getSupportFragmentManager().beginTransaction().add(fragment, fragment.getTag()).commit();
            mapFragment.getMapAsync(fragment);
        }

    }

    private void initViews() {
        this.bottomNavigationView = findViewById(R.id.homePageNavView);
        this.menuButton = findViewById(R.id.searchBarMenu);
        this.mDrawerLayout = findViewById(R.id.mapLayout);
        this.searchBar = findViewById(R.id.searchBarCardView);

        // configure navigation bar
        bottomNavigationView.setSelectedItemId(R.id.mapItemNavBar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.mapItemNavBar:
                        return true;
                    case R.id.homeItemNavBar:
                        Intent intent = new Intent(MapsActivity.this, HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.profileItemNavBar:
                        Intent intent1 = new Intent(MapsActivity.this, UserProfileActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent1);
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }
}