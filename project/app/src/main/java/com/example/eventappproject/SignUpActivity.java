package com.example.eventappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eventappproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    /* Views */
    private EditText nameET;
    private EditText emailTextRegister;
    private EditText passwordTextRegister;
    private EditText repeatPasswordTextRegister;
    private CheckBox termsCheckBox;
    private Button registerBtn;

    /* Database */
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViews();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        this.nameET = findViewById(R.id.nameET);
        this.emailTextRegister = findViewById(R.id.emailTextRegister);
        this.passwordTextRegister = findViewById(R.id.passwordTextRegister);
        this.repeatPasswordTextRegister = findViewById(R.id.repeatPasswordTextRegister);
        this.termsCheckBox = findViewById(R.id.termsCheckBox);
        this.registerBtn = findViewById(R.id.registerBtn);

        this.registerBtn.setEnabled(false);

        emailTextRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailTextRegister.getText().toString().trim().length() > 0
                        && passwordTextRegister.getText().toString().trim().length() > 0
                        && repeatPasswordTextRegister.getText().toString().trim().length() > 0) {
                    registerBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordTextRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailTextRegister.getText().toString().trim().length() > 0
                        && passwordTextRegister.getText().toString().trim().length() > 0
                        && repeatPasswordTextRegister.getText().toString().trim().length() > 0) {
                    registerBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        repeatPasswordTextRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailTextRegister.getText().toString().trim().length() > 0
                        && passwordTextRegister.getText().toString().trim().length() > 0
                        && repeatPasswordTextRegister.getText().toString().trim().length() > 0) {
                    registerBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkData()) {
                    addUserToDB();
                }
            }
        });
    }

    private void addUserToDB() {
        String nameText = nameET.getText().toString().trim();
        String emailText = emailTextRegister.getText().toString().trim();
        String passwordText = passwordTextRegister.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userID = mAuth.getCurrentUser().getUid();
                            User user = new User(userID, nameText, emailText);
                            Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(userID).setValue(user);
                            startActivity(new Intent(SignUpActivity.this, HomePageActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration was not successful!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean checkData() {
        boolean output = true;

        String emailText = emailTextRegister.getText().toString().trim();
        String passwordText = passwordTextRegister.getText().toString().trim();
        String repeatPasswordText = repeatPasswordTextRegister.getText().toString().trim();

        if (emailText.isEmpty()) {
            emailTextRegister.setError("You should input email!");
            output = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            emailTextRegister.setError("You should input valid email!");
            output = false;
        }

        if (passwordText.isEmpty() || passwordText.length() < 6) {
            passwordTextRegister.setError("Password should have length of at least 6 characters!");
            output = false;
        }

        if (repeatPasswordText.isEmpty()) {
            repeatPasswordTextRegister.setError("You should repeat your password!");
            output = false;
        } else if (!repeatPasswordText.equals(passwordText)) {
            repeatPasswordTextRegister.setError("Your password should match the field above!");
            output = false;
        }

        if (!termsCheckBox.isChecked()) {
            termsCheckBox.setError("You should agree to create an account!");
            output = false;
        }

        return output;
    }
}