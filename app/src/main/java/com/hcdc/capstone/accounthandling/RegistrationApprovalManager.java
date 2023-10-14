package com.hcdc.capstone.accounthandling;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

public class RegistrationApprovalManager {

    private final FirebaseFirestore firestore;

    public RegistrationApprovalManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void startListeningForApprovals() {
        Query query = firestore.collection("registration_requests").whereEqualTo("isApproved", true);
        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            WriteBatch batch = firestore.batch();

            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    moveUserToUsersCollection(dc.getDocument().getId(), batch);
                }
            }

            // Commit the batch
            batch.commit()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Batch write successful
                        } else {
                            // Handle error
                        }
                    });
        });
    }

    private void moveUserToUsersCollection(String userID, WriteBatch batch) {
        firestore.collection("registration_requests").document(userID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Get user data from the registration_requests collection
                        Object userData = task.getResult().getData();

                        if (userData != null) {
                            // Add the user data to the users collection
                            batch.set(firestore.collection("users").document(userID), userData);
                            // Delete the document from the registration_requests collection
                            batch.delete(firestore.collection("registration_requests").document(userID));
                        }
                    }
                });
    }
}

