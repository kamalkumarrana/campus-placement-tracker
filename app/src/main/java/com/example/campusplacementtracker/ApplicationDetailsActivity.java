package com.example.campusplacementtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ApplicationDetailsActivity extends AppCompatActivity {

    TextView tvCompany, tvRole, tvPackage, tvEligibility, tvApplied, tvInterview;
    TextView tvCandName, tvCandEmail, tvCandCgpa, tvCandTech;
    Spinner spinnerStatus;
    Button btnUpdate, btnDelete, btnBack, btnViewResume;
    View cardAdminActions, cardCandidateInfo;

    String rowId, currentStatus, studentUsername, candidateResumeUri = "";
    Database db;

    String[] statusOptions = {"Applied", "Round 1", "Round 2", "Interview Scheduled", "Selected", "Rejected"};

    boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_details);

        tvCompany = findViewById(R.id.tvDetailCompany);
        tvRole = findViewById(R.id.tvDetailRole);
        tvPackage = findViewById(R.id.tvDetailPackage);
        tvEligibility = findViewById(R.id.tvDetailEligibility);
        tvApplied = findViewById(R.id.tvDetailApplied);
        tvInterview = findViewById(R.id.tvDetailInterview);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnUpdate = findViewById(R.id.btnUpdateStatus);
        btnDelete = findViewById(R.id.btnDeleteApplication);
        btnBack = findViewById(R.id.btnDetailsBack);
        btnViewResume = findViewById(R.id.btnAdminViewResume);
        cardAdminActions = findViewById(R.id.cardAdminActions);
        cardCandidateInfo = findViewById(R.id.cardCandidateInfo);

        tvCandName = findViewById(R.id.tvCandidateName);
        tvCandEmail = findViewById(R.id.tvCandidateEmail);
        tvCandCgpa = findViewById(R.id.tvCandidateCgpa);
        tvCandTech = findViewById(R.id.tvCandidateTech);

        db = new Database(getApplicationContext());

        Intent it = getIntent();
        String company = it.getStringExtra("company");
        String role = it.getStringExtra("role");
        String pkg = it.getStringExtra("pkg");
        String eligibility = it.getStringExtra("eligibility");
        String appliedDate = it.getStringExtra("applieddate");
        String interviewDate = it.getStringExtra("interviewdate");
        String interviewTime = it.getStringExtra("interviewtime");
        currentStatus = it.getStringExtra("status");
        rowId = it.getStringExtra("rowid");
        studentUsername = it.getStringExtra("username");
        isAdmin = it.getBooleanExtra("isAdmin", false);

        tvCompany.setText(company);
        tvRole.setText("Role: " + role);
        tvPackage.setText("Package: " + pkg);
        tvEligibility.setText("Eligibility: " + eligibility);
        tvApplied.setText("Applied on: " + appliedDate);
        tvInterview.setText("Interview: " + interviewDate + " at " + interviewTime);

        // Load candidate profile if admin/HR
        if (isAdmin && studentUsername != null) {
            String[] profile = db.getProfile(studentUsername);
            // [fullname, rollno, branch, cgpa, email, role, tech_stack, resume_uri]
            cardCandidateInfo.setVisibility(View.VISIBLE);
            tvCandName.setText("Name: " + (profile[0].isEmpty() ? studentUsername : profile[0]));
            tvCandEmail.setText("Email: " + profile[4]);
            tvCandCgpa.setText("CGPA: " + profile[3]);
            tvCandTech.setText("Skills: " + (profile[6].isEmpty() ? "Not Set" : profile[6]));
            
            candidateResumeUri = profile[7];
            if (candidateResumeUri != null && !candidateResumeUri.isEmpty()) {
                btnViewResume.setVisibility(View.VISIBLE);
            }
        }

        btnViewResume.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(android.net.Uri.parse(candidateResumeUri), "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open resume", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusOptions);
        spinnerStatus.setAdapter(adapter);

        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(currentStatus)) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        // Only admin can update status
        if (!isAdmin) {
            cardAdminActions.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newStatus = spinnerStatus.getSelectedItem().toString();
                db.updateStatus(rowId, newStatus);
                Toast.makeText(getApplicationContext(), "Status updated to: " + newStatus, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ApplicationDetailsActivity.this)
                        .setTitle("Delete Application")
                        .setMessage("Are you sure you want to delete this application to " + company + "?")
                        .setPositiveButton("Delete", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                db.deleteApplication(rowId);
                                Toast.makeText(getApplicationContext(), "Application deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}