package com.example.campusplacementtracker;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class AdminEditCompanyActivity extends AppCompatActivity {

    TextView tvTitle, tvHRUser, tvHRPass;
    EditText edName, edRole, edPkg, edMinCgpa, edTech, edEligibility;
    Button btnSave, btnBack;
    String originalName;
    Database db;

    String[] degrees = {"BCA", "MCA", "B.Tech", "M.Tech", "B.Sc", "M.Sc", "B.A", "M.A", "B.Com", "M.Com", "Diploma"};
    boolean[] selectedDegrees;
    ArrayList<Integer> userSelectedDegrees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_company);

        tvTitle = findViewById(R.id.tvEditCompTitle);
        tvHRUser = findViewById(R.id.tvDispHRUsername);
        tvHRPass = findViewById(R.id.tvDispHRPasword);

        edName = findViewById(R.id.edEditCompName);
        edRole = findViewById(R.id.edEditCompRole);
        edPkg = findViewById(R.id.edEditCompPkg);
        edMinCgpa = findViewById(R.id.edEditCompMinCgpa);
        edTech = findViewById(R.id.edEditCompTechStack);
        edEligibility = findViewById(R.id.edEditCompEligibility);

        btnSave = findViewById(R.id.btnAdminUpdateComp);
        btnBack = findViewById(R.id.btnAdminEditCompBack);

        selectedDegrees = new boolean[degrees.length];
        edEligibility.setOnClickListener(v -> showDegreeDialog());

        db = new Database(getApplicationContext());

        Intent it = getIntent();
        originalName = it.getStringExtra("name");
        
        tvTitle.setText(originalName);
        edName.setText(originalName);
        edRole.setText(it.getStringExtra("role"));
        edPkg.setText(it.getStringExtra("pkg"));
        edMinCgpa.setText(it.getStringExtra("minCgpa"));
        edTech.setText(it.getStringExtra("tech"));
        String existingElig = it.getStringExtra("eligibility");
        edEligibility.setText(existingElig);

        // Pre-fill selected degrees based on string
        if (existingElig != null) {
            for (int i = 0; i < degrees.length; i++) {
                if (existingElig.contains(degrees[i])) {
                    selectedDegrees[i] = true;
                    userSelectedDegrees.add(i);
                }
            }
        }

        // Linked HR details
        String hrUsername = originalName.toLowerCase().replace(" ", "_") + "_hr";
        String pass = db.getUserPassword(hrUsername);
        tvHRUser.setText("HR Username: " + hrUsername);
        tvHRPass.setText("HR Password: " + (pass.isEmpty() ? "Not Found" : pass));

        btnSave.setOnClickListener(v -> {
            String name = edName.getText().toString();
            String role = edRole.getText().toString();
            String pkg = edPkg.getText().toString();
            String minCgpa = edMinCgpa.getText().toString();
            String tech = edTech.getText().toString();
            String eligibility = edEligibility.getText().toString();

            if (name.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "Name and Role are required", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(AdminEditCompanyActivity.this)
                .setTitle("Confirm Update")
                .setMessage("Save changes to " + originalName + "?")
                .setPositiveButton("Save", (dialog, which) -> {
                    db.updateCompany(originalName, name, role, pkg, eligibility, tech, minCgpa, "");
                    Toast.makeText(this, "Company updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void showDegreeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Eligible Degrees");
        builder.setMultiChoiceItems(degrees, selectedDegrees, (dialogInterface, i, isChecked) -> {
            if (isChecked) {
                userSelectedDegrees.add(i);
            } else {
                userSelectedDegrees.remove(Integer.valueOf(i));
            }
        });

        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < userSelectedDegrees.size(); j++) {
                stringBuilder.append(degrees[userSelectedDegrees.get(j)]);
                if (j != userSelectedDegrees.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
            edEligibility.setText(stringBuilder.toString());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
