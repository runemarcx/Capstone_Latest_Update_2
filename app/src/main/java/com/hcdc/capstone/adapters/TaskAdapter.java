package com.hcdc.capstone.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hcdc.capstone.R;
import com.hcdc.capstone.taskprocess.TaskData;
import com.hcdc.capstone.taskprocess.TaskDetails;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    Context context;
    ArrayList<TaskData> list;

    public TaskAdapter(Context context, ArrayList<TaskData> list) {
        this.context = context;
        this.list = list;
        }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.tasklayout,parent,false);
        return new TaskViewHolder(v);
        }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskData taskData = list.get(position);
        holder.tasktitle.setText(taskData.getTaskName());
        holder.taskpoint.setText(taskData.getPoints() + " points");
        holder.taskloc.setText(taskData.getLocation());
        holder.taskdesc.setText(taskData.getDescription());

        // Check if the task has a timeFrame
        if (taskData.getHours() > 0 || taskData.getMinutes() > 0) {
            String timeFrameText = "Time Frame: " + taskData.getHours() + " hours " + taskData.getMinutes() + " minutes";
            holder.taskTimer.setText(timeFrameText);
            holder.taskTimer.setVisibility(View.VISIBLE);
            } else {
            holder.taskTimer.setVisibility(View.GONE);
            }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    TaskData selectedTask = list.get(clickedPosition);

                    // Create an intent to open the TaskDetails activity
                    Intent intent = new Intent(context, TaskDetails.class);
                    intent.putExtra("tasktitle", selectedTask.getTaskName());
                    intent.putExtra("taskdetails", selectedTask.getDescription());
                    intent.putExtra("taskpoint", selectedTask.getPoints());
                    intent.putExtra("tasklocation", selectedTask.getLocation());
                    intent.putExtra("taskDuration", "Hours: " + selectedTask.getHours() + " Minutes: " + selectedTask.getMinutes());

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder{

        TextView tasktitle, taskdesc, taskpoint, taskloc, taskTimer;
        public TaskViewHolder(@NonNull View taskView)
            {
            super(taskView);

            tasktitle = taskView.findViewById(R.id.taskTitle);
            taskdesc = taskView.findViewById(R.id.taskDesc);
            taskpoint = taskView.findViewById(R.id.taskPoint);
            taskloc = taskView.findViewById(R.id.taskLocation);
            taskTimer = taskView.findViewById(R.id.taskTimeFrame);
            }
    }
}