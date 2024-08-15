package com.wsofts.attendance;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Settings extends AppCompatActivity {

    private EditText firstnameEditText, surnameEditText, emailEditText, passwordEditText;
    private Button updateButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        firstnameEditText = findViewById(R.id.firstname);
        surnameEditText = findViewById(R.id.surname);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        updateButton = findViewById(R.id.update_button);

        // Load current user data
        loadUserData();

        // Handle update button click
        updateButton.setOnClickListener(v -> updateUserInformation());
    }

    private void loadUserData() {
        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());
            db.collection("Lecturer").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            firstnameEditText.setText(documentSnapshot.getString("firstname"));
                            surnameEditText.setText(documentSnapshot.getString("surname"));
                        }
                    });
        }
    }

    private void updateUserInformation() {
        String firstname = firstnameEditText.getText().toString().trim();
        String surname = surnameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstname) || TextUtils.isEmpty(surname) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            Map<String, Object> lecturer = new HashMap<>();
            lecturer.put("firstname", firstname);
            lecturer.put("surname", surname);
            lecturer.put("email", email);

            db.collection("Lecturer").document(currentUser.getUid())
                    .update(lecturer)
                    .addOnSuccessListener(aVoid -> {
                        currentUser.updateEmail(email)
                                .addOnSuccessListener(aVoid1 -> {
                                    if (!TextUtils.isEmpty(password)) {
                                        currentUser.updatePassword(password)
                                                .addOnSuccessListener(aVoid2 ->
                                                        Toast.makeText(Settings.this, "Information updated successfully", Toast.LENGTH_SHORT).show())
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(Settings.this, "Failed to update password. Try again.", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(Settings.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(Settings.this, "Failed to update email. Try again.", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(Settings.this, "Failed to update information. Try again.", Toast.LENGTH_SHORT).show());
        }
    }
}
