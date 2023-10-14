package com.hcdc.capstone.transactionprocess;

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
import com.hcdc.capstone.adapters.RewardCompleteAdapter;
import com.hcdc.capstone.rewardprocess.CouponsModel;
import java.util.ArrayList;
import java.util.Objects;

public class RewardCompleteFragment extends Fragment {

    private RecyclerView recyclerView;
    private RewardCompleteAdapter adapter;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RewardCompleteAdapter(requireContext(), new ArrayList<>());

        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.completedreward_fragment, container, false);

        recyclerView = view.findViewById(R.id.completedrewardfragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Fetch claimed rewards from Firestore
        fetchCompletedRewardsFromFirestore();

        return view;
    }

    private void fetchCompletedRewardsFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        firestore.collection("complete_rewardreq") // Replace with your Firestore collection name for completed rewards
                .whereEqualTo("isDoneClaimed", true).whereEqualTo("userId", currentUserUID) // Fetch only claimed rewards
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<CouponsModel> claimedRewards = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CouponsModel coupon = document.toObject(CouponsModel.class);
                            claimedRewards.add(coupon);
                        }
                        adapter.setFinRewardList(claimedRewards);
                    }

                });
    }
}
