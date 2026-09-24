package com.example.campusplacementtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    TextView tvWelcome, tvDispFullName, tvDispEmail, tvDispRollNo, tvDispBranch, tvDispCgpa, tvCgpaReqStatus, tvRequestCgpaPopup, tvDispTechStack;
    EditText edFullName, edRollNo, edTechStack;
    Spinner spinnerBranch;
    Button btnSaveProfile, btnGoCompanies, btnGoApplications, btnLogout, btnThemeToggle, btnEnableEdit, btnCancelEdit, btnChangePass, btnUploadResume, btnViewResume;
    LinearLayout layoutView, layoutEdit;
    String username, resumeUriStr = "";
    Database db;

    private ActivityResultLauncher<String[]> resumePicker;

    String[] degrees = {"BCA", "MCA", "B.Tech", "M.Tech", "B.Sc", "M.Sc", "B.A", "M.A", "B.Com", "M.Com", "Diploma"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        resumePicker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                resumeUriStr = uri.toString();
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    // Handle non-persistable URIs or permission issues
                }
                Toast.makeText(this, "Resume selected", Toast.LENGTH_SHORT).show();
            }
        });

        tvWelcome = findViewById(R.id.tvWelcome);
        // ...
        tvDispFullName = findViewById(R.id.tvDispFullName);
        tvDispEmail = findViewById(R.id.tvDispEmail);
        tvDispRollNo = findViewById(R.id.tvDispRollNo);
        tvDispBranch = findViewById(R.id.tvDispBranch);
        tvDispCgpa = findViewById(R.id.tvDispCgpa);
        tvCgpaReqStatus = findViewById(R.id.tvCgpaReqStatus);
        tvRequestCgpaPopup = findViewById(R.id.tvRequestCgpaPopup);
        tvDispTechStack = findViewById(R.id.tvDispTechStack);

        edFullName = findViewById(R.id.edFullName);
        edRollNo = findViewById(R.id.edRollNo);
        edTechStack = findViewById(R.id.edTechStack);
        spinnerBranch = findViewById(R.id.spinnerBranch);

        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, degrees);
        spinnerBranch.setAdapter(branchAdapter);

        layoutView = findViewById(R.id.layoutProfileView);
        layoutEdit = findViewById(R.id.layoutProfileEdit);

        btnEnableEdit = findViewById(R.id.btnEnableEdit);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnGoCompanies = findViewById(R.id.btnGoCompanies);
        btnGoApplications = findViewById(R.id.btnGoApplications);
        btnChangePass = findViewById(R.id.btnStudentChangePass);
        btnLogout = findViewById(R.id.btnLogout);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        btnUploadResume = findViewById(R.id.btnUploadResume);
        btnViewResume = findViewById(R.id.btnViewResume);

        SharedPreferences sp = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        username = sp.getString("username", "");
        tvWelcome.setText("Welcome, " + username);

        db = new Database(getApplicationContext());

        loadProfile();
        updateThemeButton();

        btnEnableEdit.setOnClickListener(v -> {
            layoutView.setVisibility(View.GONE);
            layoutEdit.setVisibility(View.VISIBLE);
        });

        btnCancelEdit.setOnClickListener(v -> {
            layoutEdit.setVisibility(View.GONE);
            layoutView.setVisibility(View.VISIBLE);
        });

        btnUploadResume.setOnClickListener(v -> resumePicker.launch(new String[]{"application/pdf"}));

        btnViewResume.setOnClickListener(v -> {
            if (resumeUriStr.isEmpty()) {
                Toast.makeText(this, "No resume uploaded", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(resumeUriStr), "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open resume", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvDispCgpa.setOnClickListener(v -> showCgpaRequestDialog());
        tvRequestCgpaPopup.setOnClickListener(v -> showCgpaRequestDialog());

        btnChangePass.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class)));

        btnSaveProfile.setOnClickListener(v -> {
            String fullname = edFullName.getText().toString();
            String rollno = edRollNo.getText().toString();
            String branch = spinnerBranch.getSelectedItem().toString();
            String tech = edTechStack.getText().toString();

            if (fullname.isEmpty() || rollno.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter at least Name and Roll Number", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                .setTitle("Save Changes")
                .setMessage("Are you sure you want to update your profile?")
                .setPositiveButton("Save", (dialog, which) -> {
                    db.updateProfile(username, fullname, rollno, branch, tvDispCgpa.getText().toString(), tech, resumeUriStr);
                    Toast.makeText(getApplicationContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    loadProfile();
                    layoutEdit.setVisibility(View.GONE);
                    layoutView.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        btnGoCompanies.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, CompanyListActivity.class)));
        btnGoApplications.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, MyApplicationsActivity.class)));
        btnThemeToggle.setOnClickListener(v -> {
            ThemeHelper.toggleTheme(ProfileActivity.this);
            recreate();
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.apply();
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
        });
    }

    private void loadProfile() {
        String[] profile = db.getProfile(username);
        // [fullname, rollno, branch, cgpa, email, role, tech_stack, resume_uri]
        String branchVal = profile[2];
        tvDispFullName.setText(profile[0].isEmpty() ? "Not Set" : profile[0]);
        tvDispRollNo.setText(profile[1].isEmpty() ? "Not Set" : profile[1]);
        tvDispBranch.setText(branchVal.isEmpty() ? "Not Set" : branchVal);
        tvDispCgpa.setText(profile[3]);
        tvDispEmail.setText(profile[4]);
        tvDispTechStack.setText(profile[6].isEmpty() ? "Not Set" : profile[6]);
        resumeUriStr = profile[7];
        
        if (resumeUriStr.isEmpty()) {
            btnViewResume.setVisibility(View.GONE);
        } else {
            btnViewResume.setVisibility(View.VISIBLE);
        }

        String reqStatus = db.getLatestCgpaRequestStatus(username);
        if (reqStatus != null && !reqStatus.isEmpty()) {
            tvCgpaReqStatus.setText("Latest Request: " + reqStatus);
        } else {
            tvCgpaReqStatus.setText("No pending requests");
        }

        edFullName.setText(profile[0]);
        edRollNo.setText(profile[1]);
        edTechStack.setText(profile[6]);

        // Set spinner selection based on stored branch
        if (!branchVal.isEmpty()) {
            for (int i = 0; i < degrees.length; i++) {
                if (degrees[i].equalsIgnoreCase(branchVal)) {
                    spinnerBranch.setSelection(i);
                    break;
                }
            }
        }
    }

    private void showCgpaRequestDialog() {
        EditText edRequest = new EditText(this);
        edRequest.setHint("Enter your actual CGPA");
        edRequest.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(this)
                .setTitle("Request CGPA Update")
                .setMessage("Submit your CGPA for admin verification.")
                .setView(edRequest)
                .setPositiveButton("Submit Request", (dialog, which) -> {
                    String val = edRequest.getText().toString().trim();
                    if (!val.isEmpty()) {
                        db.addCgpaRequest(username, val);
                        Toast.makeText(this, "Request sent to Admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateThemeButton() {
        if (ThemeHelper.isDarkMode(this)) {
            btnThemeToggle.setText("☀ Light Mode");
        } else {
            btnThemeToggle.setText("🌙 Dark Mode");
        }
    }
}
