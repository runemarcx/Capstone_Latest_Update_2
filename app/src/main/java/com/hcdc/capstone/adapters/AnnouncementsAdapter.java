package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.AnnouncementModel;
import com.hcdc.capstone.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnounceViewHolder> {

    Context announceContext;
    ArrayList<AnnouncementModel> announceList;

    public AnnouncementsAdapter(Context announceContext, ArrayList<AnnouncementModel> announceList)
    {
        this.announceContext = announceContext;
        this.announceList = announceList;
    }

    @NonNull
    @Override
    public AnnouncementsAdapter.AnnounceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(announceContext).inflate(R.layout.announcement_card, parent, false);
        return new AnnouncementsAdapter.AnnounceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementsAdapter.AnnounceViewHolder holder, int position) {
        AnnouncementModel announcementModel = announceList.get(position);

        // Set the click listener for the card view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomDialog(announcementModel); // Pass the clicked AnnouncementModel
            }
        });

        holder.announceTitle.setText(announcementModel.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        String formattedDate = dateFormat.format(announcementModel.getTimestamp());
        holder.announceTime.setText("Posted on: "+formattedDate);
    }

    @Override
    public int getItemCount() {
        if (announceList != null) {
            return announceList.size();
        } else {
            return 0; // Return 0 if the list is null
        }
    }

    public static class AnnounceViewHolder extends RecyclerView.ViewHolder{

        TextView announceTitle, announceTime;
        public AnnounceViewHolder(@NonNull View itemView) {
            super(itemView);

            announceTitle = itemView.findViewById(R.id.announce_title);
            announceTime = itemView.findViewById(R.id.announce_post_time);


        }
    }

    private void showCustomDialog(AnnouncementModel announcementModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(announceContext);
        View dialogView = LayoutInflater.from(announceContext).inflate(R.layout.announce_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Find the views in the custom dialog
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogDescription = dialogView.findViewById(R.id.dialog_description);
        Button closeButton = dialogView.findViewById(R.id.close_button);

        // Set the title and description in the dialog
        dialogTitle.setText(announcementModel.getTitle());
        dialogDescription.setText(announcementModel.getDescription());

        // Close the dialog when the "Close" button is clicked
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
