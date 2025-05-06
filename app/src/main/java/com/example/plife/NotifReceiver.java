package com.example.plife;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
public class NotifReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "appointment_reminder_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract data from the intent
        String appointmentTitle = intent.getStringExtra("appointmentTitle");

        // Create and show the notification
        createNotification(context, "Appointment Reminder", "Your appointment is in 3 days!", appointmentTitle);
    }

    private void createNotification(Context context, String title, String message, String appointmentId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent to send a broadcast to NotifReceiver
        Intent notificationIntent = new Intent(context, NotifReceiver.class);
        notificationIntent.setAction("OPEN_NOTIFICATION_FRAGMENT");
        notificationIntent.putExtra("appointmentId", appointmentId);

        // Create a PendingIntent for the notification
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);  // Automatically dismiss the notification when clicked

        // Display the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}
