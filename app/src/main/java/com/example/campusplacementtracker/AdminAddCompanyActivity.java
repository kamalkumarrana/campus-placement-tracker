package com.example.campusplacementtracker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;

public class AdminAddCompanyActivity extends AppCompatActivity {

    EditText edName, edRole, edPkg, edEligibility, edMinCgpa, edTechStack;
    Button btnAdd, btnBack, btnPickLogo;
    ImageView ivLogoPreview;
    Database db;
    String logoUriStr = "";

    private ActivityResultLauncher<String[]> logoPicker;

    String[] degrees = {"BCA", "MCA", "B.Tech", "M.Tech", "B.Sc", "M.Sc", "B.A", "M.A", "B.Com", "M.Com", "Diploma"};
    boolean[] selectedDegrees;
    ArrayList<Integer> userSelectedDegrees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_company);

        logoPicker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                logoUriStr = uri.toString();
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    // Handle non-persistable URIs
                }
                ivLogoPreview.setImageURI(uri);
                ivLogoPreview.setVisibility(View.VISIBLE);
            }
        });

        edName = findViewById(R.id.edCompName);
        edRole = findViewById(R.id.edCompRole);
        edPkg = findViewById(R.id.edCompPkg);
        edMinCgpa = findViewById(R.id.edCompMinCgpa);
        edTechStack = findViewById(R.id.edCompTechStack);
        edEligibility = findViewById(R.id.edCompEligibility);
        btnAdd = findViewById(R.id.btnAddCompany);
        btnBack = findViewById(R.id.btnAddCompBack);
        btnPickLogo = findViewById(R.id.btnPickLogo);
        ivLogoPreview = findViewById(R.id.ivCompLogoPreview);

        selectedDegrees = new boolean[degrees.length];
        edEligibility.setOnClickListener(v -> showDegreeDialog());

        db = new Database(getApplicationContext());

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edName.getText().toString();
                String role = edRole.getText().toString();
                String pkg = edPkg.getText().toString();
                String eligibility = edEligibility.getText().toString();
                String minCgpa = edMinCgpa.getText().toString();
                String techStack = edTechStack.getText().toString();

                if (name.isEmpty() || role.isEmpty() || pkg.isEmpty() || eligibility.isEmpty() || minCgpa.isEmpty() || techStack.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double cgpa = Double.parseDouble(minCgpa);
                    if (cgpa < 0 || cgpa > 10) {
                        Toast.makeText(getApplicationContext(), "CGPA must be between 0 and 10", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Invalid CGPA format", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(AdminAddCompanyActivity.this)
                    .setTitle("Confirm Add")
                    .setMessage("Are you sure you want to add this company?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.addCompany(name, role, pkg, eligibility, techStack, minCgpa, logoUriStr);
                        
                        // Automatically create a company user account
                        String hrUsername = name.toLowerCase().replace(" ", "_") + "_hr";
                        String hrPassword = name.toLowerCase().replace(" ", "") + "123";
                        if (!db.usernameExists(hrUsername)) {
                            db.registerUser(hrUsername, "hr@" + name.toLowerCase().replace(" ", "") + ".com", hrPassword, "company");
                            Toast.makeText(getApplicationContext(), "Company & HR account (" + hrUsername + ") created", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Company added successfully", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnPickLogo.setOnClickListener(v -> logoPicker.launch(new String[]{"image/*"}));
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

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }
}
