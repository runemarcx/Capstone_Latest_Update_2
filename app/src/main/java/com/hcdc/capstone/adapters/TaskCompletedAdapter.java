package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.R;
import com.hcdc.capstone.transactionprocess.TaskCompleteData;

import java.util.ArrayList;

public class TaskCompletedAdapter extends RecyclerView.Adapter<TaskCompletedAdapter.TaskCompletedViewHolder>{

        Context completedContext;
        ArrayList<TaskCompleteData> completelist;

        public TaskCompletedAdapter(Context completedContext, ArrayList<TaskCompleteData> completelist)
        {
            this.completedContext = completedContext;
            this.completelist = completelist;
        }

    @NonNull
    @Override
    public TaskCompletedAdapter.TaskCompletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(completedContext).inflate(R.layout.cardtransacttasks,parent,false);
       return new TaskCompletedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskCompletedAdapter.TaskCompletedViewHolder holder, int position) {

    TaskCompleteData data = completelist.get(position);
    holder.comTaskname.setText(data.getTaskName());
    holder.comTaskLoc.setText(data.getLocation());
    holder.comTaskPoint.setText("+"+data.getPoints());


    }

    @Override
    public int getItemCount() {
        return completelist.size();
    }

    public static class TaskCompletedViewHolder extends RecyclerView.ViewHolder{

            TextView comTaskname, comTaskLoc, comTaskPoint;
        public TaskCompletedViewHolder(@NonNull View itemView) {
            super(itemView);

            comTaskname = itemView.findViewById(R.id.transtaskName);
            comTaskLoc = itemView.findViewById(R.id.translocation);
            comTaskPoint = itemView.findViewById(R.id.transpointsadded);
        }
    }
}
