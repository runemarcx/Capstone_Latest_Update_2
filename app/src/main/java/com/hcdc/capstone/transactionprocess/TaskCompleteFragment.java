package com.hcdc.capstone.transactionprocess;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.TaskCompletedAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskCompleteFragment extends Fragment {

    private TaskCompletedAdapter taskCompletedAdapter;
    private FirebaseAuth auth;
    private List<TaskCompleteData> completedTaskList;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.completedtask_fragment, container, false);
        auth = FirebaseAuth.getInstance();
        // Initialize RecyclerView
        RecyclerView completedTaskRecyclerView = view.findViewById(R.id.completedtaskfragment);
        completedTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list to store completed tasks
        completedTaskList = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView
        taskCompletedAdapter = new TaskCompletedAdapter(getContext(), (ArrayList<TaskCompleteData>) completedTaskList);
        completedTaskRecyclerView.setAdapter(taskCompletedAdapter);

        // Fetch completed tasks from Firestore
        fetchCompletedTasksFromFirestore();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchCompletedTasksFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        firestore.collection("completed_task") // Replace with your Firestore collection name
                .whereEqualTo("isConfirmed", true).whereEqualTo("acceptedBy",currentUserUID) // Fetch only confirmed tasks
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskName = document.getString("taskName");
                            String location = document.getString("location");
                            String points = document.getString("points");
                            boolean isConfirmed = Boolean.TRUE.equals(document.getBoolean("isConfirmed"));

                            TaskCompleteData taskCompleteData = new TaskCompleteData(taskName, location, points, isConfirmed);
                            completedTaskList.add(taskCompleteData);
                        }

                        // Notify the adapter that data has changed
                        taskCompletedAdapter.notifyDataSetChanged();
                    }  // Handle the error if data retrieval fails

                });
    }
}
