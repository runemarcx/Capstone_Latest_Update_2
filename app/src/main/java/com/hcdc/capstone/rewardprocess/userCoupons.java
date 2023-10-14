package com.hcdc.capstone.rewardprocess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.CouponAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class userCoupons extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private CouponAdapter couponAdapter;
    private ArrayList<CouponsModel> couponList;

    private TextView progCode, progRewardName;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_coupons);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        progCode = findViewById(R.id.coupCodeProgress);
        progRewardName = findViewById(R.id.coupRewardNameProgress);

        //Coupon List
        RecyclerView couponRecyclerView = findViewById(R.id.couponRecyclerView);
        couponList = new ArrayList<>();
        couponAdapter = new CouponAdapter(this, couponList);
        fetchCoupons();
        fetchprogressCoupons();

        couponRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        couponRecyclerView.setAdapter(couponAdapter);

        CardView coupProgress = findViewById(R.id.cardViewProgress);
        coupProgress.setOnClickListener(v -> {
            if(progCode.getText().toString().equals("No reward requested..."))
            {
                Toast.makeText(getApplicationContext(),"Reward in progress is empty",Toast.LENGTH_LONG).show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(userCoupons.this);
                builder.setTitle("Cancel Reward"); // Set the title of the dialog
                builder.setMessage("Do you want to cancel this reward?"); // Set the message

                // Add a "Yes" button
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    // Handle the "Yes" button click here
                    // You can add your cancel reward logic here
                    cancelButton();
                    dialog.dismiss(); // Dismiss the dialog
                });

                // Add a "No" button
                builder.setNegativeButton("No", (dialog, which) -> {
                    // Handle the "No" button click here, if needed
                    dialog.dismiss(); // Dismiss the dialog
                });

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });

    }

    private void cancelButton() {
        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        WriteBatch batch = firestore.batch();
        firestore.collection("rewardrequest")
                .whereEqualTo("userId", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null && !documents.isEmpty()) {
                            for (QueryDocumentSnapshot document : documents) {
                                batch.delete(document.getReference());
                            }
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(userCoupons.this, " Reward request canceled successfully ", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(userCoupons.this, Reward.class);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        }
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void fetchprogressCoupons() {
        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        firestore.collection("rewardrequest")
                .whereEqualTo("userId", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Assuming you only expect one document, you can access the first one
                            QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                            String rewardName = documentSnapshot.getString("rewardName");
                            String couponuserCode = documentSnapshot.getString("couponuserCode");

                            // Add debug logs to check values
                            Log.d("ProgressCouponName", "rewards fetched: " + rewardName);
                            Log.d("ProgressCouponCode", "coupon fetched: " + couponuserCode);

                            progCode.setText(couponuserCode);
                            progRewardName.setText(rewardName);
                        } else {
                            progCode.setText("No reward requested...");
                            progRewardName.setText("");
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            // Handle the error here
                            Log.e("FirestoreError", "Error fetching reward request: " + exception.getMessage());
                        }
                    }
                });
    }


    @SuppressLint("NotifyDataSetChanged")
    private void fetchCoupons() {
        // Clear the existing list
        couponList.clear();

        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        firestore.collection("complete_rewardreq")
                .whereEqualTo("userId", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot document : documents) {
                                String rewardName = document.getString("rewardName");
                                String userCouponCode = document.getString("couponuserCode");
                                boolean status = Boolean.TRUE.equals(document.getBoolean("pendingStatus"));
                                String useremail = document.getString("email");
                                boolean isClaimable = Boolean.TRUE.equals(document.getBoolean("isClaimable"));

                                // Check if the coupon is claimable
                                if (isClaimable) {
                                    // Check the type of rewardPoints and convert it to int
                                    Object rewardPointsObj = document.get("rewardPoints");
                                    int rewardPoints = 0; // Default value if not found or conversion fails
                                    if (rewardPointsObj != null) {
                                        if (rewardPointsObj instanceof Long) {
                                            rewardPoints = ((Long) rewardPointsObj).intValue();
                                        } else if (rewardPointsObj instanceof Integer) {
                                            rewardPoints = (int) rewardPointsObj;
                                        }
                                    }

                                    CouponsModel coupon = new CouponsModel(rewardName, currentUserUID, status, useremail, rewardPoints, userCouponCode);
                                    couponList.add(coupon);

                                    Log.d("CouponFetch", "Reward Name: " + rewardName + ", Coupon Code: " + userCouponCode);
                                }
                            }

                            // Notify the adapter that data has changed
                            couponAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Reward.class);
        startActivity(intent);
        finish();
    }

}
