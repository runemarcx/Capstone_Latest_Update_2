package com.hcdc.capstone;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {

    // Define a unique channel ID
    public static final String CHANNEL_ID = "my_notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the notification channel when the app starts
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Task Timer Service Channel",
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}


