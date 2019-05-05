package com.namseoul.sa.tab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class LocationService extends Service {

    Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        context = this;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel notificationChannel = new NotificationChannel("id","name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription("channel description");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100,200,100,200});
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder builder = new Notification.Builder(context,"id")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("타이틀")
                .setContentText("내용");

        notificationManager.notify(0,builder.build());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }
}
