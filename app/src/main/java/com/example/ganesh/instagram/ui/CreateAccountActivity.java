package com.example.ganesh.instagram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ganesh.instagram.MainActivity;
import com.example.ganesh.instagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {

    private final String TAG = CreateAccountActivity.class.getName();
    private EditText mUsername, mEmail, mPassword;
    private Button mCreateAccount;
    ProgressBar mAuthProgressBar;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String username, email, password;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        initializeScreen();

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean isSuccessful = createAccount();
                if (!isSuccessful) {
                    showAuthToast("Something is wrong!");
                }
            }
        });

    }

    private Boolean createAccount() {
        username = mUsername.getText().toString().trim();
        email = mEmail.getText().toString().trim();
        password = mPassword.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password);

        if (!validEmail && !validPassword) {
            return false;
        } else {
            mAuthProgressBar.setVisibility(View.VISIBLE);
            mCreateAccount.setVisibility(View.INVISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mAuthProgressBar.setVisibility(View.INVISIBLE);
                        userId = mAuth.getCurrentUser().getUid();

                        Boolean isUserCreatedInFirebase = createUserInFirebase();
                        if (!isUserCreatedInFirebase) {
                            showAuthToast("Failed to create a Firebase user");
                            return;
                        }

                        showAuthToast("Register Successful");

                        Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        mAuthProgressBar.setVisibility(View.INVISIBLE);
                        finish();

                    }
                }
            });
            return true;
        }

    }

    public void showAuthToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private Boolean createUserInFirebase() {
        if (userId.equals("")) {
            showAuthToast("User is not created!");
            return false;
        }
        DatabaseReference userReference = databaseReference.child(userId);
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userReference.setValue(userMap);
        return true;

    }

    private boolean isValidPassword(String password) {
        if (password.equals("")) {
            mPassword.setError("Email cannot be empty!");
            return false;
        }
        if (password.length() < 6) {
            mPassword.setError("Password lenght should be > 6");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        if (email.equals("")) {
            mEmail.setError("Email cannot be empty!");
            return false;
        }
        return true;
    }

    private void initializeScreen() {
        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mCreateAccount = findViewById(R.id.create_account_btn);

        mAuthProgressBar = findViewById(R.id.progressBar);
        mAuthProgressBar.setVisibility(View.INVISIBLE);
    }

}
