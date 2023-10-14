package com.hcdc.capstone.accounthandling;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {
    public static final String TAG = "TAG";
    private FirebaseAuth auth;
    private FirebaseFirestore fstore;
    private EditText regName, regEmail, regBrgy, regPassword, regConfirmpass;
    private Button regButton;
    private TextView regRedirect;
    private String userID;
    private RegistrationApprovalManager approvalManager;
    private WriteBatch writeBatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        regName = findViewById(R.id.register_username);
        regEmail = findViewById(R.id.register_email);
        regBrgy = findViewById(R.id.register_barangay);
        regPassword = findViewById(R.id.register_password);
        regConfirmpass = findViewById(R.id.confirmReg_password);
        regButton = findViewById(R.id.registerbtn);
        regRedirect = findViewById(R.id.registerRedirect);

        // Initialize the WriteBatch
        writeBatch = fstore.batch();

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Rname = regName.getText().toString();
                String Remail = regEmail.getText().toString().trim();
                String Rbrgy = regBrgy.getText().toString();
                String Rpass = regPassword.getText().toString().trim();

                if (Rname.isEmpty()) {
                    regName.setError("Name cannot be empty");
                    return;
                }
                if (Remail.isEmpty()) {
                    regEmail.setError("Email cannot be empty");
                    return;
                }
                if (Rbrgy.isEmpty()) {
                    regBrgy.setError("Barangay cannot be empty");
                    return;
                }
                if (Rpass.isEmpty()) {
                    regPassword.setError("Password cannot be empty");
                    return;
                }
                if (!Rpass.equals(regConfirmpass.getText().toString())) {
                    regConfirmpass.setError("Passwords do not match");
                    return;
                }
                if(!Remail.contains("@gmail.com")){
                    regEmail.setError("Only accepts with Gmail addresses");
                    return;
                }

                auth.createUserWithEmailAndPassword(Remail, Rpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            userID = auth.getCurrentUser().getUid();
                            DocumentReference documentReference = fstore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", regName.getText().toString());
                            user.put("Barangay", regBrgy.getText().toString());
                            user.put("email", regEmail.getText().toString());
                            user.put("Uid", userID);

                            // Set isApproved to false initially for new users
                            user.put("isApproved", false);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Success: user profile is created for" + userID);
                                }
                            });

                            // After successful registration, add registration data to registration_requests collection
                            Map<String, Object> registrationData = new HashMap<>();
                            registrationData.put("name", Rname);
                            registrationData.put("Barangay", Rbrgy);
                            registrationData.put("email", Remail);
                            registrationData.put("isApproved", false);
                            registrationData.put("Uid", userID);
                            DocumentReference registrationRequestRef = fstore.collection("registration_requests").document(userID);
                            registrationRequestRef.set(registrationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Success: user registration request saved for" + userID);
                                    // Show a message to the user that their registration is pending approval
                                    Toast.makeText(RegisterActivity.this, "Registration request sent for approval", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Commit the batch to execute Firestore writes atomically
                            writeBatch.commit()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Batch write successful
                                            Log.d(TAG, "Batch write successful");
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle batch write failure
                                            Log.e(TAG, "Error in batch write: " + e.getMessage());
                                            Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Sign Up Failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        regRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        // Start listening for registration approvals
        approvalManager = new RegistrationApprovalManager();
        approvalManager.startListeningForApprovals();
    }
}
