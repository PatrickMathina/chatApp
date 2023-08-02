package com.patsofts.chatapp.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.patsofts.chatapp.MessageActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "my_channel_id";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            updateToken(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");
        String user = remoteMessage.getData().get("user");

        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentUser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sented.equals(firebaseUser.getUid())) {
            if (!currentUser.equals(user)) {
                sendNotification(remoteMessage);
            }
        }
    }

    private void updateToken(String token) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
        Token newToken = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(newToken);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int x = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, x, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(Integer.parseInt(icon))
//                .setSmallIcon(R.drawable.notification_icon) // R.mipmap.ic_launcher
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Creating the notification channel (for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "My Channel";
            String channelDescription = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);

            // Registering the channel with the system
            notificationManager.createNotificationChannel(channel);
        }

        int i = Math.max(x, 0);
        // i.e
//        int i = 0;
//        if (x > 0) {
//            i = x;
//        }
        notificationManager.notify(i, builder.build());
    }
}


//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.media.RingtoneManager;
//import android.net.Uri;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.NotificationCompat;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//import com.patsofts.chatapp.MessageActivity;
//
//public class MyFirebaseMessaging extends FirebaseMessagingService {
//
//    @Override
//    public void onNewToken(@NonNull String token) {
//        super.onNewToken(token);
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            updateToken(token);
//        }
//    }
//
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//
//        String sented = remoteMessage.getData().get("sented");
//
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (firebaseUser != null && sented.equals(firebaseUser.getUid())) {
//            sendNotification(remoteMessage);
//        }
//    }
//
//    private void updateToken(String token) {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
//        Token newToken = new Token(token);
//        reference.child(firebaseUser.getUid()).setValue(newToken);
//    }
//
//    private void sendNotification(RemoteMessage remoteMessage) {
//        String user = remoteMessage.getData().get("user");
//        String icon = remoteMessage.getData().get("icon");
//        String title = remoteMessage.getData().get("title");
//        String body = remoteMessage.getData().get("body");
//
//        RemoteMessage.Notification notification = remoteMessage.getNotification();
//        int x = Integer.parseInt(user.replaceAll("[\\D]", ""));
//        Intent intent = new Intent(this, MessageActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("userid", user);
//        intent.putExtras(bundle);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, x, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                .setSmallIcon(Integer.parseInt(icon))
//                .setContentTitle(title)
//                .setAutoCancel(true)
//                .setSound(defaultSound)
//                .setContentIntent(pendingIntent);
//        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        int i = Math.max(x, 0);
//
//        nm.notify(i, builder.build());
//    }
//}

// i.e
//        int i = 0;
//        if (x > 0) {
//            i = x;
//        }