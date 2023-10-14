package com.hcdc.capstone.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.RewardRequest;
import com.hcdc.capstone.rewardprocess.RewardsModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder>{

    Context Rcontext;
    ArrayList<RewardsModel> Rlist;

    AlertDialog alertDialog;

    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public RewardAdapter(Context Rcontext, ArrayList<RewardsModel> Rlist) {
        this.Rcontext = Rcontext;
        this.Rlist = Rlist;
    }

    @NonNull
    @Override
    public RewardAdapter.RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(Rcontext).inflate(R.layout.rewardslayout, parent, false);
        return new RewardAdapter.RewardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardAdapter.RewardViewHolder holder, int position) {
        RewardsModel rewardsModel = Rlist.get(position);
        holder.rewardname.setText(rewardsModel.getRewardName());
        holder.rewardpoint.setText(rewardsModel.getPoints() + " points");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder rewardBuilder = new AlertDialog.Builder(Rcontext);
                View rewardPopup = LayoutInflater.from(Rcontext).inflate(R.layout.reward_dialog, null);

                TextView rwrd = rewardPopup.findViewById(R.id.userRemainingPoints);
                TextView rwrdtitle = rewardPopup.findViewById(R.id.getRewardTitle);
                TextView rwrdpoint = rewardPopup.findViewById(R.id.getRewardPoint);

                AppCompatImageButton closerwrd = rewardPopup.findViewById(R.id.rewardclose);
                AppCompatButton reqrwrd = rewardPopup.findViewById(R.id.requestReward);

                rwrdtitle.setText(rewardsModel.getRewardName());
                rwrdpoint.setText("Required points to claim: " + rewardsModel.getPoints() + " points");

                rewardBuilder.setView(rewardPopup);
                alertDialog = rewardBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();

                // Fetch user's points from Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(currentUserId);
                userRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();
                        if (userData != null && userData.containsKey("userpoints")) {
                            Long userPointsLong = (Long) userData.get("userpoints");
                            int userPoints = userPointsLong != null ? userPointsLong.intValue() : 0;
                            rwrd.setText("Points Available:  " + userPoints);
                        }
                    }
                }).addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(Rcontext, "Failed to fetch user points.", Toast.LENGTH_SHORT).show();
                });

                closerwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                reqrwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Fetch user's points from Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("users").document(currentUserId);

                        userRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Map<String, Object> userData = documentSnapshot.getData();
                                if (userData != null && userData.containsKey("userpoints")) {
                                    Long userPointsLong = (Long) userData.get("userpoints");
                                    int userPoints = userPointsLong != null ? userPointsLong.intValue() : 0;

                                    int requiredPoints = Integer.parseInt(rewardsModel.getPoints());

                                    if (userPoints >= requiredPoints) {
                                        // Check for existing pending reward requests
                                        db.collection("rewardrequest")
                                                .whereEqualTo("userId", currentUserId)
                                                .whereEqualTo("pendingStatus", true)
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    if (!querySnapshot.isEmpty()) {
                                                        // User already has a pending request
                                                        AlertDialog.Builder pendingRequestDialog = new AlertDialog.Builder(Rcontext);
                                                        pendingRequestDialog.setTitle("Pending Request");
                                                        pendingRequestDialog.setMessage("You already have a pending reward request. Please wait for it to be accepted.");
                                                        pendingRequestDialog.setPositiveButton("OK", (dialog, which) -> {
                                                            dialog.dismiss();
                                                        });
                                                        pendingRequestDialog.create().show();
                                                    } else {
                                                        // Check the number of completed reward requests
                                                        db.collection("complete_rewardreq")
                                                                .whereEqualTo("userId", currentUserId)
                                                                .get()
                                                                .addOnSuccessListener(completedReqSnapshot -> {
                                                                    if (completedReqSnapshot.size() < 3) {
                                                                        // User has fewer than 3 completed requests, generate a unique coupon code
                                                                        generateUniqueCouponCode(db, rewardsModel);
                                                                    } else {
                                                                        // User has reached the limit of completed requests
                                                                        AlertDialog.Builder limitReachedDialog = new AlertDialog.Builder(Rcontext);
                                                                        limitReachedDialog.setTitle("Request Limit Reached");
                                                                        limitReachedDialog.setMessage("You have reached the maximum limit of completed reward requests.");
                                                                        limitReachedDialog.setPositiveButton("OK", (dialog, which) -> {
                                                                            dialog.dismiss();
                                                                        });
                                                                        limitReachedDialog.create().show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    // Handle failure
                                                                    Toast.makeText(Rcontext, "Failed to check completed reward requests.", Toast.LENGTH_SHORT).show();
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle failure
                                                    Toast.makeText(Rcontext, "Failed to check existing requests.", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // User has insufficient points
                                        AlertDialog.Builder insufficientPointsDialog = new AlertDialog.Builder(Rcontext);
                                        insufficientPointsDialog.setTitle("Insufficient Points");
                                        insufficientPointsDialog.setMessage("You do not have enough points to claim this reward.");
                                        insufficientPointsDialog.setPositiveButton("OK", (dialog, which) -> {
                                            dialog.dismiss();
                                        });
                                        insufficientPointsDialog.create().show();
                                    }
                                }
                            }
                        }).addOnFailureListener(e -> {
                            // Handle failure
                            Toast.makeText(Rcontext, "Failed to fetch user points.", Toast.LENGTH_SHORT).show();
                        });
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return Rlist.size();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder{

        TextView rewardname, rewardpoint;
        public RewardViewHolder(@NonNull View rewardView) {
            super(rewardView);
            rewardname = rewardView.findViewById(R.id.rewardTitle);
            rewardpoint = rewardView.findViewById(R.id.rewardPoint);
        }
    }

    private String generateCouponCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder couponCode = new StringBuilder(length);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            couponCode.append(randomChar);
        }
        String generatedCode = couponCode.toString();
        Log.d("CouponGeneration", "Generated Coupon Code: " + generatedCode); // Add this log
        return generatedCode;
    }

    // Method to generate a unique coupon code
    private void generateUniqueCouponCode(FirebaseFirestore db, RewardsModel rewardsModel) {
        String couponCode = generateCouponCode(11);

        // Check if the coupon code already exists in Firestore
        db.collection("rewardrequest")
                .whereEqualTo("couponCode", couponCode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // The coupon code is unique, proceed with adding the reward request
                        addRewardRequestToFirestore(db, rewardsModel, couponCode);
                    } else {
                        // The coupon code already exists, generate a new one
                        generateUniqueCouponCode(db, rewardsModel);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(Rcontext, "Failed to check coupon code uniqueness.", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to add reward request to Firestore
    private void addRewardRequestToFirestore(FirebaseFirestore db, RewardsModel rewardsModel, String couponCode) {
        // Batch write: Add reward request and update user's points
        WriteBatch batch = db.batch();
        DocumentReference rewardRequestRef = db.collection("rewardrequest").document();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        int rewardPoints = Integer.parseInt(rewardsModel.getPoints()); // Get reward points
        batch.set(rewardRequestRef, new RewardRequest(rewardsModel.getRewardName(), currentUserId, true, userEmail, rewardPoints, couponCode));

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Display a success dialog after the batch write
                    AlertDialog.Builder successDialog = new AlertDialog.Builder(Rcontext);
                    successDialog.setTitle("Reward Request Successful");
                    successDialog.setMessage("Your reward request has been submitted. Please wait for it to be processed.");
                    successDialog.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        alertDialog.dismiss();  // Dismiss the original reward dialog
                    });
                    successDialog.create().show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(Rcontext, "Reward request failed.", Toast.LENGTH_SHORT).show();
                });
    }
}



/*old code
*
* reqrwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Fetch user's points from Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("users").document(currentUserId);
                        userRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Map<String, Object> userData = documentSnapshot.getData();
                                if (userData != null && userData.containsKey("userpoints")) {
                                    Long userPointsLong = (Long) userData.get("userpoints");
                                    int userPoints = userPointsLong != null ? userPointsLong.intValue() : 0;

                                    int requiredPoints = Integer.parseInt(rewards.getPoints());

                                    if (userPoints >= requiredPoints) {
                                        // Check for existing pending reward requests
                                        db.collection("rewardrequest")
                                                .whereEqualTo("userId", currentUserId)
                                                .whereEqualTo("pendingStatus", true)
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    if (!querySnapshot.isEmpty()) {
                                                        // Generate a unique coupon code
                                                        generateUniqueCouponCode(db, rewards);
                                                    } else {
                                                        // User already has a pending request
                                                        AlertDialog.Builder pendingRequestDialog = new AlertDialog.Builder(Rcontext);
                                                        pendingRequestDialog.setTitle("Pending Request");
                                                        pendingRequestDialog.setMessage("You already have a pending reward request. Please wait for it to be accepted.");
                                                        pendingRequestDialog.setPositiveButton("OK", (dialog, which) -> {
                                                            dialog.dismiss();
                                                        });
                                                        pendingRequestDialog.create().show();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle failure
                                                    Toast.makeText(Rcontext, "Failed to check existing requests.", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // User has insufficient points
                                        AlertDialog.Builder insufficientPointsDialog = new AlertDialog.Builder(Rcontext);
                                        insufficientPointsDialog.setTitle("Insufficient Points");
                                        insufficientPointsDialog.setMessage("You do not have enough points to claim this reward.");
                                        insufficientPointsDialog.setPositiveButton("OK", (dialog, which) -> {
                                            dialog.dismiss();
                                        });
                                        insufficientPointsDialog.create().show();
                                    }
                                }
                            }
                        }).addOnFailureListener(e -> {
                            // Handle failure
                            Toast.makeText(Rcontext, "Failed to fetch user points.", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
* */
