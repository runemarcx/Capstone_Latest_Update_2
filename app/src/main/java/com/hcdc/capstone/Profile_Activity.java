package com.hcdc.capstone;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcdc.capstone.accounthandling.LoginActivity;

public class Profile_Activity extends BaseActivity {

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView barangayTextView;
    private TextView pointsTextView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameTextView = findViewById(R.id.name);
        emailTextView = findViewById(R.id.user_email);
        barangayTextView = findViewById(R.id.user_barangay);
        pointsTextView = findViewById(R.id.points_system);
        logoutButton = findViewById(R.id.logoutbtn);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        logoutButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Profile_Activity.this);
            builder.setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(Profile_Activity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String barangay = documentSnapshot.getString("Barangay");
                        Long userPoints = documentSnapshot.getLong("userpoints");

                        if (name != null) {
                            nameTextView.setText(name);
                        }

                        if (email != null) {
                            emailTextView.setText(email);
                        }

                        if (barangay != null) {
                            barangayTextView.setText(barangay);
                        }

                        if (userPoints != null) {
                            pointsTextView.setText(String.valueOf(userPoints));
                        }
                    }
                });
    }
}
