package com.example.campusplacementtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AdminCreateUserActivity extends AppCompatActivity {

    EditText edUsername, edEmail, edPassword;
    Spinner spinnerBranch;
    Button btnCreate, btnBack;
    Database db;

    String[] degrees = {"BCA", "MCA", "B.Tech", "M.Tech", "B.Sc", "M.Sc", "B.A", "M.A", "B.Com", "M.Com", "Diploma"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_user);

        edUsername = findViewById(R.id.edAdminCreateUsername);
        edEmail = findViewById(R.id.edAdminCreateEmail);
        spinnerBranch = findViewById(R.id.spinnerAdminCreateBranch);
        edPassword = findViewById(R.id.edAdminCreatePassword);
        btnCreate = findViewById(R.id.btnAdminCreateUser);
        btnBack = findViewById(R.id.btnAdminCreateBack);

        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, degrees);
        spinnerBranch.setAdapter(branchAdapter);

        db = new Database(getApplicationContext());

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = edUsername.getText().toString();
                String email = edEmail.getText().toString();
                String branch = spinnerBranch.getSelectedItem().toString();
                String pass = edPassword.getText().toString();

                if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ValidationHelper.isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ValidationHelper.isValidPassword(pass)) {
                    Toast.makeText(getApplicationContext(), "Password must be 6+ chars with 1 digit and 1 special char", Toast.LENGTH_LONG).show();
                    return;
                }

                if (db.usernameExists(user)) {
                    Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(AdminCreateUserActivity.this)
                    .setTitle("Confirm Creation")
                    .setMessage("Create student account for " + user + "?")
                    .setPositiveButton("Create", (dialog, which) -> {
                        db.registerUser(user, email, pass, "student");
                        db.updateProfile(user, "", "", branch, "0", "", ""); // Added resume param
                        Toast.makeText(getApplicationContext(), "Student account created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
