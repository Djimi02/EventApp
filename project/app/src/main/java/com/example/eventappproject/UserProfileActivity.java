package com.example.eventappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventappproject.interfaces.UserDataListener;
import com.example.eventappproject.models.User;
import com.example.eventappproject.repositories.UserDataRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileActivity extends AppCompatActivity implements UserDataListener {

    /* Views */
    private BottomNavigationView bottomNavigationView;
    private TextView helloUserTV;
    private TextView userNameTV;
    private TextView userEmailTV;
    private TextView userDescTV;
    private Button updateDataBTN;
    private Button logoutBTN;
    private Button deleteAccountBTN;
    private ConstraintLayout constraintLayout;

    /* Variables */
    private User user;

    /* Database */
    private DatabaseReference dbReferenceUsers;
    private DatabaseReference dbReferenceEvents;
    private UserDataRepository userDataRepository;

    /* Dialog */
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    /* Delete user dialog views */
    private EditText passwordDeleteUser;
    private Button deleteUserDeleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initVars();
        initViews();
    }

    private void initViews() {
        this.constraintLayout = findViewById(R.id.userProfileConstrLayout);
        this.bottomNavigationView = findViewById(R.id.homePageNavView);
        this.helloUserTV = findViewById(R.id.helloUserTV);
        this.userNameTV = findViewById(R.id.userNameTV);
        this.userEmailTV = findViewById(R.id.userEmailTV);
        this.userDescTV = findViewById(R.id.userDescriptionTV);
        this.updateDataBTN = findViewById(R.id.updateDataBTN);
        this.logoutBTN = findViewById(R.id.logoutBTN);
        this.deleteAccountBTN = findViewById(R.id.deleteAccountBTN);

        if (this.user != null) {
            helloUserTV.setText("Hello, " + user.getName() + "!");
            userNameTV.setText("Name: " + user.getName());
            userEmailTV.setText("Email: " + user.getEmail());
            userDescTV.setText("Description: " + user.getDescription());
        }

        // Configure navigation bar
        bottomNavigationView.setSelectedItemId(R.id.profileItemNavBar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.mapItemNavBar:
                        startActivity(new Intent(UserProfileActivity.this, MapsActivity.class));
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

        // Set functionality to logout btn
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginPage();
            }
        });

        // Set delete account btn func
        deleteAccountBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnackBar();;
            }
        });
    }

    private void initVars() {
        this.userDataRepository = UserDataRepository.getInstance();
        userDataRepository.addListener(this);

        this.user = userDataRepository.getUser();

        dbReferenceUsers = FirebaseDatabase.getInstance("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        dbReferenceEvents = FirebaseDatabase.getInstance("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Events");

    }

    /**
     * Logs out current user, redirects to login page, clears the back stack and deletes the data
     * for the current user.
     */
    private void goToLoginPage() {
        Intent intent = new Intent(UserProfileActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
        finish();
        UserDataRepository.deleteCurrentInstance(); // deletes the data for the user
    }

    private void showSnackBar() {
        Snackbar.make(constraintLayout, "Are you sure?", Snackbar.LENGTH_INDEFINITE).setAction("Confirm", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        }).show();
    }

    private void deleteUser() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.delete_user_dialog, null);

        this.passwordDeleteUser = popupView.findViewById(R.id.passwordDeleteUserDialog);
        this.deleteUserDeleteDialog = popupView.findViewById(R.id.deleteEventDeleteUserDialog);

        deleteUserDeleteDialog.setEnabled(false);

        passwordDeleteUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordDeleteUser.getText().toString().trim();
                if (password.length() < 6) {
                    deleteUserDeleteDialog.setEnabled(false);
                } else {
                    deleteUserDeleteDialog.setEnabled(true);
                }
            }
        });

        deleteUserDeleteDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

                String password = passwordDeleteUser.getText().toString().trim();

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

                if (fUser == null) {
                    return;
                }

                fUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            System.out.println(user.getEmail() + " " + password);
                            passwordDeleteUser.setError("Incorrect Password");
                            return;
                        }

                        userDataRepository.deleteAllEvents(); // deletes all created events

                        userDataRepository.deleteAllEvents(); // leaves all joined events

                        fUser.delete(); // deletes authentication details about the user

                        dbReferenceUsers.child(user.getDbID()).setValue(null); // deletes user object in db

                        dialog.dismiss();
                        goToLoginPage();
                        Toast.makeText(UserProfileActivity.this, "Account was deleted successfully.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // show dialog
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    @Override
    public void updateUser() {
        this.user = userDataRepository.getUser();

        if (this.user != null) {
            helloUserTV.setText("Hello, " + user.getName() + "!");
            userNameTV.setText("Name: " + user.getName());
            userEmailTV.setText("Email: " + user.getEmail());
            userDescTV.setText("Description: " + user.getDescription());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationView.setSelectedItemId(R.id.profileItemNavBar);
    }
}