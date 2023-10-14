package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.CouponsModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RewardCompleteAdapter extends RecyclerView.Adapter<RewardCompleteAdapter.RewardViewHolder> {

    private final Context finRewardContext;
    private ArrayList<CouponsModel> finRewardList;

    public RewardCompleteAdapter(Context finRewardContext, ArrayList<CouponsModel> finRewardList) {
        this.finRewardContext = finRewardContext;
        this.finRewardList = finRewardList;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(finRewardContext).inflate(R.layout.cardtransactreward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        CouponsModel couponsModel = finRewardList.get(position);
        holder.finRewardCode.setText(couponsModel.getCouponuserCode());
        holder.finRewardName.setText(couponsModel.getRewardName());

        // Format the Timestamp to "MM/DD/YYYY HH:MM" and set it to TextView
        String formattedClaimDate = formatTimestamp(couponsModel.getClaimDate());
        holder.finClaimDate.setText(formattedClaimDate);
    }

    @Override
    public int getItemCount() {
        return finRewardList.size();
    }

    public void setFinRewardList(ArrayList<CouponsModel> claimedRewards) {
        finRewardList = claimedRewards;
        notifyDataSetChanged();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder {

        TextView finRewardName, finRewardCode, finClaimDate;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            finRewardCode = itemView.findViewById(R.id.fincoupCode);
            finRewardName = itemView.findViewById(R.id.fincoupRewardName);
            finClaimDate = itemView.findViewById(R.id.coupRewardDate);
        }
    }

    // Helper method to format Timestamp
    private String formatTimestamp(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy (hh:mma)", Locale.US);
        return sdf.format(new Date(timestamp.getSeconds() * 1000));
    }
}
