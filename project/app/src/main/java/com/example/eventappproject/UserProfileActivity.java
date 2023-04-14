package com.example.eventappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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

import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity implements UserDataListener {

    /* Views */
    private BottomNavigationView bottomNavigationView;
    private TextView helloUserTV;
    private TextView userNameTV;
    private TextView userEmailTV;
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

    /* Update user data dialog views */
    private EditText newNameET;
    private Button updateNewNameBTN;
    private EditText newEmailET;
    private EditText newEmailCurrentPasswordET;
    private Button updateNewEmailBTN;
    private EditText newPasswordET;
    private EditText repeatNewPasswordET;
    private EditText newPasswordCurrentPasswordET;
    private Button updateNewPasswordBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removes title bar from ProfileSettingsActivity
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

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
        this.updateDataBTN = findViewById(R.id.updateDataBTN);
        this.logoutBTN = findViewById(R.id.logoutBTN);
        this.deleteAccountBTN = findViewById(R.id.deleteAccountBTN);

        if (this.user != null) {
            helloUserTV.setText("Hello, " + user.getName() + "!");
            userNameTV.setText("Name: " + user.getName());
            userEmailTV.setText("Email: " + user.getEmail());
        }

        // Configure navigation bar
        bottomNavigationView.setSelectedItemId(R.id.profileItemNavBar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.mapItemNavBar:
                        Intent int1 = new Intent(UserProfileActivity.this, MapsActivity.class);
                        int1.putExtra("getLocation", "false");
                        startActivity(int1);
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

        updateDataBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangeUserDataDialog();
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
        FirebaseAuth.getInstance().signOut();
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

                        userDataRepository.leaveAllEvents(); // leaves all joined events

                        fUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dbReferenceUsers.child(user.getDbID()).setValue(null); // deletes user object in db
                            }
                        }); // deletes authentication details about the user

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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    public void updateUser() {
        this.user = userDataRepository.getUser();

        if (this.user != null) {
            helloUserTV.setText("Hello, " + user.getName() + "!");
            userNameTV.setText("Name: " + user.getName());
            userEmailTV.setText("Email: " + user.getEmail());
        }
    }

    private void openChangeUserDataDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.update_user_data, null);

        changeNewNameFunctionality(popupView);
        changeNewEmailFunctionality(popupView);
        changeNewPasswordFunctionality(popupView);

        // show dialog
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void changeNewNameFunctionality(View popupView) {
        this.newNameET = popupView.findViewById(R.id.editTextTextPersonName);
        this.updateNewNameBTN = popupView.findViewById(R.id.button);

        updateNewNameBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = newNameET.getText().toString().trim();
                if (newName.isEmpty()) {
                    newNameET.setError("Input new name!");
                    return;
                }

                user.setName(newName);
                dbReferenceUsers.child(user.getDbID()).setValue(user);

                dialog.dismiss();
                Toast.makeText(UserProfileActivity.this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeNewEmailFunctionality(View popupView) {
        this.newEmailET = popupView.findViewById(R.id.editTextTextPersonName2);
        this.newEmailCurrentPasswordET = popupView.findViewById(R.id.editTextTextPassword2);
        this.updateNewEmailBTN = popupView.findViewById(R.id.button2);

        updateNewEmailBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = newEmailET.getText().toString().trim();
                String currentPassword = newEmailCurrentPasswordET.getText().toString().trim();

                if (newEmail.isEmpty()) {
                    newEmailET.setError("You should input email!");
                    return;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    newEmailET.setError("You should input valid email!");
                    return;
                }

                if (currentPassword.isEmpty()) {
                    newEmailCurrentPasswordET.setError("Input current password!");
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                fUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(UserProfileActivity.this, "Wrong credentials", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        user.setEmail(newEmail);
                        dbReferenceUsers.child(user.getDbID()).setValue(user);
                        fUser.updateEmail(newEmail);
                        dialog.dismiss();
                        Toast.makeText(UserProfileActivity.this, "Email updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void changeNewPasswordFunctionality(View popupView) {
        this.newPasswordET = popupView.findViewById(R.id.editTextTextPassword3);
        this.repeatNewPasswordET = popupView.findViewById(R.id.editTextTextPassword4);
        this.newPasswordCurrentPasswordET = popupView.findViewById(R.id.editTextTextPassword5);
        this.updateNewPasswordBTN = popupView.findViewById(R.id.button3);
        
        updateNewPasswordBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                String newPassword = newPasswordET.getText().toString().trim();
                String repeatNewPassword = repeatNewPasswordET.getText().toString().trim();
                String currentPassword = newPasswordCurrentPasswordET.getText().toString().trim();

                if (newPassword.isEmpty() || newPassword.length() < 6) {
                    newPasswordET.setError("Password should have length of at least 6 characters!");
                    return;
                }
                if (repeatNewPassword.isEmpty()) {
                    repeatNewPasswordET.setError("You should repeat your password!");
                    return;
                } else if (!repeatNewPassword.equals(newPassword)) {
                    repeatNewPasswordET.setError("Your password should match the field above!");
                    return;
                }
                if (currentPassword.isEmpty()) {
                    newPasswordCurrentPasswordET.setError("Input current password!");
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                fUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        fUser.updatePassword(newPassword);
                        dialog.dismiss();
                        Toast.makeText(UserProfileActivity.this, "Password updated successfully1", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationView.setSelectedItemId(R.id.profileItemNavBar);
    }
}