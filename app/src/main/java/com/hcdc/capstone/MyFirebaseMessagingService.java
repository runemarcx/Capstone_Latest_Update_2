package com.hcdc.capstone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle the incoming notification here, if needed
        // You can add custom logic to process incoming messages
        // For this code, we're only sending notifications, not processing incoming messages
        //sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        //Log.d("lanyer", "onMessageReceived: " + remoteMessage.getData().get("title"));
        //Log.d("lanyer", "onMessageReceived: " + remoteMessage.getData().get("body"));
    }

    private void sendNotification(String title, String messageBody) {
        String channelId = "MyChannelId"; // You can change this channel ID
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and higher
        String channelName = "MyChannelName"; // You can change this channel name
        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
