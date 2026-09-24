package com.example.campusplacementtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AdminEditUserActivity extends AppCompatActivity {

    TextView tvTitle;
    EditText edFullName, edRollNo, edCgpa, edTechStack;
    Spinner spinnerStatus, spinnerBranch;
    Button btnSave, btnBack, btnManualApply, btnResetPassword;
    String username;
    Database db;

    String[] statusOptions = {"active", "inactive"};
    String[] degrees = {"BCA", "MCA", "B.Tech", "M.Tech", "B.Sc", "M.Sc", "B.A", "M.A", "B.Com", "M.Com", "Diploma"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        tvTitle = findViewById(R.id.tvEditUserTitle);
        edFullName = findViewById(R.id.edAdminFullName);
        edRollNo = findViewById(R.id.edAdminRollNo);
        spinnerBranch = findViewById(R.id.spinnerAdminBranch);
        edCgpa = findViewById(R.id.edAdminCgpa);
        edTechStack = findViewById(R.id.edAdminTechStack);
        spinnerStatus = findViewById(R.id.spinnerAccountStatus);
        btnSave = findViewById(R.id.btnAdminSaveUser);
        btnBack = findViewById(R.id.btnAdminEditBack);
        btnManualApply = findViewById(R.id.btnAdminManualApply);
        btnResetPassword = findViewById(R.id.btnAdminResetPassword);

        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, degrees);
        spinnerBranch.setAdapter(branchAdapter);

        db = new Database(getApplicationContext());
        // ... rest of init ...

        Intent it = getIntent();
        username = it.getStringExtra("username");
        tvTitle.setText("Edit: " + username);
        edFullName.setText(it.getStringExtra("fullname"));
        edRollNo.setText(it.getStringExtra("rollno"));
        edCgpa.setText(it.getStringExtra("cgpa"));
        edTechStack.setText(it.getStringExtra("tech_stack"));
        String branchVal = it.getStringExtra("branch");
        String currentStatus = it.getStringExtra("status");

        if (branchVal != null && !branchVal.isEmpty()) {
            for (int i = 0; i < degrees.length; i++) {
                if (degrees[i].equalsIgnoreCase(branchVal)) {
                    spinnerBranch.setSelection(i);
                    break;
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusOptions);
        spinnerStatus.setAdapter(adapter);

        if (currentStatus != null && currentStatus.equals("inactive")) {
            spinnerStatus.setSelection(1);
        } else {
            spinnerStatus.setSelection(0);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullname = edFullName.getText().toString();
                String rollno = edRollNo.getText().toString();
                String branch = spinnerBranch.getSelectedItem().toString();
                String cgpa = edCgpa.getText().toString();
                String status = spinnerStatus.getSelectedItem().toString();
                String tech = edTechStack.getText().toString();

                new AlertDialog.Builder(AdminEditUserActivity.this)
                    .setTitle("Confirm Update")
                    .setMessage("Save changes to " + username + "'s profile?")
                    .setPositiveButton("Save", (dialog, which) -> {
                        db.adminUpdateUser(username, fullname, rollno, branch, cgpa, status, tech, ""); // Added resume param
                        Toast.makeText(getApplicationContext(), "Student profile updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });

        btnManualApply.setOnClickListener(v -> {
            Intent intent = new Intent(AdminEditUserActivity.this, AdminManualApplyActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnResetPassword.setOnClickListener(v -> showResetPasswordDialog());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showResetPasswordDialog() {
        EditText edNewPassword = new EditText(this);
        edNewPassword.setHint("Enter New Password");
        edNewPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Set a new password for " + username)
                .setView(edNewPassword)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newPass = edNewPassword.getText().toString().trim();
                    if (newPass.isEmpty()) {
                        Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!ValidationHelper.isValidPassword(newPass)) {
                        Toast.makeText(this, "Invalid Format (6+ chars, 1 digit, 1 special)", Toast.LENGTH_LONG).show();
                        return;
                    }
                    db.updatePassword(username, newPass);
                    Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
