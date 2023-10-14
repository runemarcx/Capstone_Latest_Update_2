package com.hcdc.capstone.taskprocess;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskDetails extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView tskTitle, tskPoint, tskDesc, tskLoc, tskDura;

    private String uID, userEmail;

    private Button acceptTask, cancelTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tskTitle = findViewById(R.id.tdTitle);
        tskDesc = findViewById(R.id.tdDesc);
        tskPoint = findViewById(R.id.tdPoints);
        tskLoc = findViewById(R.id.tdLocation);
        tskDura = findViewById(R.id.tdDuration);

        acceptTask = findViewById(R.id.tdAccept);
        cancelTask = findViewById(R.id.tdCancel);

        Bundle extra = getIntent().getExtras();
        String tTitle = extra.getString("tasktitle");
        String tDesc = extra.getString("taskdetails");
        String tPoints = extra.getString("taskpoint");
        String tLoc = extra.getString("tasklocation");
        String tDura = extra.getString("taskDuration");

        tskTitle.setText(tTitle);
        tskDesc.setText(tDesc);
        tskLoc.setText(tLoc);
        tskPoint.setText(tPoints);
        tskDura.setText(tDura);

        acceptTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uID = auth.getCurrentUser().getUid();

                firestore.collection("user_acceptedTask")
                        .whereEqualTo("acceptedBy", uID)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                showAcceptConfirmationOverlay();
                            } else {
                                Log.d(TAG, "User has already accepted a task.");
                                Toast.makeText(TaskDetails.this, " You have already accepted a task ", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error checking accepted tasks", e);
                        });
            }
        });

        cancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Task.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void acceptNewTask() {
        WriteBatch batch = firestore.batch();

        firestore.collection("tasks")
                .whereEqualTo("taskName", tskTitle.getText().toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        batch.update(documentSnapshot.getReference(), "isAccepted", true);

                        Map<String, Object> userTaskAccepted = new HashMap<>();
                        userTaskAccepted.put("taskName", tskTitle.getText().toString());
                        userTaskAccepted.put("description", tskDesc.getText().toString());
                        userTaskAccepted.put("location", tskLoc.getText().toString());
                        userTaskAccepted.put("points", tskPoint.getText().toString());
                        userTaskAccepted.put("isAccepted", true);
                        userTaskAccepted.put("isStarted", false);
                        userTaskAccepted.put("isCompleted", false);
                        userTaskAccepted.put("isConfirmed", false);
                        userTaskAccepted.put("acceptedBy", uID);
                        userTaskAccepted.put("acceptedByEmail", userEmail);

                        if (documentSnapshot.contains("timeFrame")) {
                            userTaskAccepted.put("timeFrame", documentSnapshot.get("timeFrame"));
                        }

                        long currentTimeMillis = System.currentTimeMillis();
                        String formattedDate = formatDateTime(currentTimeMillis);
                        userTaskAccepted.put("acceptedDateTime", formattedDate);

                        batch.delete(documentSnapshot.getReference());
                        batch.set(firestore.collection("user_acceptedTask").document(), userTaskAccepted);

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Batch write successful");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Batch write failed", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting tasks for update", e);
                });
    }

    private String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a MM/dd/yy");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private void showAcceptConfirmationOverlay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View overlayView = getLayoutInflater().inflate(R.layout.accept_confirmation_overlay, null);
        Button confirmButton = overlayView.findViewById(R.id.confirmButton);
        Button cancelButton = overlayView.findViewById(R.id.cancelButton);

        builder.setView(overlayView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                userEmail = auth.getCurrentUser().getEmail();
                acceptNewTask();

                Intent intent = new Intent(getApplicationContext(), Task.class);
                intent.putExtra("navigateToMyTasks", true);
                startActivity(intent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
