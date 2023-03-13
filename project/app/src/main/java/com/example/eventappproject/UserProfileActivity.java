package com.example.eventappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class UserProfileActivity extends AppCompatActivity {

    /* Views */
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        initVars();
    }

    private void initViews() {
        this.bottomNavigationView = findViewById(R.id.homePageNavView);

        // configure navigation bar
        bottomNavigationView.setSelectedItemId(R.id.profileItemNavBar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.mapItemNavBar:
                        Toast.makeText(UserProfileActivity.this, "Map selected!", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.homeItemNavBar:
                        Intent intent = new Intent(UserProfileActivity.this, HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.profileItemNavBar:

                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void initVars() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationView.setSelectedItemId(R.id.profileItemNavBar);
    }
}