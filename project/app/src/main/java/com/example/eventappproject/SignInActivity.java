package com.example.eventappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private TextView goToRegistration;
    private EditText emailTextLogin;
    private EditText passwordTextLogin;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removes title bar from ProfileSettingsActivity
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_sign_in);

        initViews();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        checkIfLogged();
    }

    private void checkIfLogged() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        Toast.makeText(SignInActivity.this, "Login was successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
        finish();
    }

    private void initViews() {
        this.goToRegistration = findViewById(R.id.registerView);
        this.btnLogin = findViewById(R.id.btnLogin);
        this.emailTextLogin = findViewById(R.id.emailTextLogin);
        this.passwordTextLogin = findViewById(R.id.passwordTextLogin);

        btnLogin.setEnabled(false);

        emailTextLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (emailTextLogin.getText().toString().trim().length() > 0 && passwordTextLogin.getText().toString().trim().length() > 0) {
                    btnLogin.setEnabled(true);
                }
            }
        });

        passwordTextLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordTextLogin.getText().toString().trim().length() > 0 || emailTextLogin.getText().toString().trim().length() > 0) {
                    btnLogin.setEnabled(true);
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkData()){
                    loginUser();
                }
            }
        });

        goToRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });
    }

    private boolean checkData() {
        String email = this.emailTextLogin.getText().toString().trim();
        String password = this.passwordTextLogin.getText().toString().trim();

        boolean output = true;

        if (email.isEmpty()) {
            this.emailTextLogin.setError("You should input your email!");
            output = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.emailTextLogin.setError("You should input valid email!");
            output = false;
        }

        if (password.isEmpty() || password.length() < 6) {
            this.passwordTextLogin.setError("Password should have length of at least 6 characters!");
            output = false;
        }
        return output;
    }

    private void loginUser() {
        String email = this.emailTextLogin.getText().toString().trim();
        String password = this.passwordTextLogin.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, "Login was successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignInActivity.this, "Something went wrong! Please check your credentials!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}