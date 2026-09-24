package com.example.campusplacementtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView tvTotalApps, tvSelectedApps, tvTotalUsers, tvTotalCompanies;
    Button btnManageApplications, btnManageUsers, btnAddCompany, btnManageSlots, btnViewSlots, btnLogout, btnAdminCreateUser, btnAdminViewCompanies, btnCgpaRequests, btnChangePass, btnExport;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotalApps = findViewById(R.id.tvTotalApps);
        tvSelectedApps = findViewById(R.id.tvSelectedApps);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalCompanies = findViewById(R.id.tvTotalCompanies);

        btnManageApplications = findViewById(R.id.btnManageApplications);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnAddCompany = findViewById(R.id.btnAddCompany);
        btnManageSlots = findViewById(R.id.btnManageSlots);
        btnViewSlots = findViewById(R.id.btnAdminViewSlots);
        btnCgpaRequests = findViewById(R.id.btnAdminCgpaRequests);
        btnAdminCreateUser = findViewById(R.id.btnAdminCreateUser);
        btnAdminViewCompanies = findViewById(R.id.btnAdminViewCompanies);
        btnChangePass = findViewById(R.id.btnAdminChangePass);
        btnExport = findViewById(R.id.btnExportReport);
        btnLogout = findViewById(R.id.btnAdminLogout);

        db = new Database(getApplicationContext());

        loadStats();

        btnManageApplications.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminManageApplicationsActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminUserListActivity.class)));
        btnAdminCreateUser.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminCreateUserActivity.class)));
        btnAdminViewCompanies.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminCompanyListActivity.class)));
        btnAddCompany.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminAddCompanyActivity.class)));
        btnManageSlots.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminManageSlotsActivity.class)));
        btnViewSlots.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ViewSlotsActivity.class)));
        btnCgpaRequests.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminCgpaRequestsActivity.class)));
        btnChangePass.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ChangePasswordActivity.class)));
        btnExport.setOnClickListener(v -> exportData());

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(AdminDashboardActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences sp = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.apply();
                        startActivity(new Intent(AdminDashboardActivity.this, MainActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            }
        });
    }

    private void loadStats() {
        ArrayList<String> apps = db.getAllApplications();
        int pendingCount = 0;
        int selectedCount = 0;
        for (String app : apps) {
            String[] parts = app.split("\\|");
            if (parts.length < 8) continue;
            String status = parts[7];
            if (status.contains("Selected")) {
                selectedCount++;
            } else if (status.contains("Applied") || status.contains("Round")) {
                pendingCount++;
            }
        }
        
        int totalStudents = db.getAllUsers().size();
        int totalComps = db.getCompanies().size();

        tvTotalApps.setText(String.valueOf(pendingCount));
        tvSelectedApps.setText(String.valueOf(selectedCount));
        tvTotalUsers.setText(String.valueOf(totalStudents));
        tvTotalCompanies.setText(String.valueOf(totalComps));
    }

    private void exportData() {
        ArrayList<String> apps = db.getAllApplications();
        StringBuilder csv = new StringBuilder("Company,Role,Package,Student,Status\n");
        for (String app : apps) {
            String[] p = app.split("\\|");
            if (p.length < 10) continue;
            csv.append(p[0]).append(",").append(p[1]).append(",").append(p[2]).append(",").append(p[9]).append(",").append(p[7]).append("\n");
        }
        
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Placement Report");
            intent.putExtra(Intent.EXTRA_TEXT, csv.toString());
            startActivity(Intent.createChooser(intent, "Share Placement Report"));
        } catch (Exception e) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }
}
