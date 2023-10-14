package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.CouponsModel;

import java.util.ArrayList;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    Context coupContext;
    ArrayList<CouponsModel> coupList;

    public CouponAdapter(Context coupContext,  ArrayList<CouponsModel> coupList)
    {
        this.coupContext = coupContext;
        this.coupList = coupList;
    }
    @NonNull
    @Override
    public CouponAdapter.CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(coupContext).inflate(R.layout.couponlayout,parent,false);
        return new CouponAdapter.CouponViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponAdapter.CouponViewHolder holder, int position) {
        CouponsModel couponsModel = coupList.get(position);

        holder.coupName.setText(couponsModel.getRewardName());
        holder.coupCod.setText(couponsModel.getCouponuserCode());
    }

    @Override
    public int getItemCount() {
        return coupList.size(); // Return the size of your coupList
    }


    public static class CouponViewHolder extends RecyclerView.ViewHolder{

        TextView coupCod, coupName;
        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);

            coupCod = itemView.findViewById(R.id.coupCode);
            coupName= itemView.findViewById(R.id.coupRewardName);
        }
    }
}
