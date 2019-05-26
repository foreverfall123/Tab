package com.namseoul.sa.tab;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ParentService extends Service {

    private static final String CHANNEL_ID = "channel_01";

    static final int PORT = 10001;
    IBinder mBinder = new MyBinder();

    Socket socket;
    DataInputStream is;
    DataOutputStream os;
    String msg="";
    String ip = null;

    private Gson gson;
    private SharedPreferences sp;
    private List<GPSData> datas;
    private GPSData GPSData;

    double lat,lon;

    String[] s;

    boolean startrealgps = false;
    boolean stoprealgps = false;

    String check;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public class MyBinder extends Binder {
        ParentService getMyService(){
            return ParentService.this;
        }
    }
    @Override
    public boolean onUnbind(Intent intent){
        return true;
    }

    @Override
    public IBinder onBind(Intent intent){

        new Thread(new Runnable() {
            @Override
            public void run() {

                while(ip == null){
                    try{
                        Thread.sleep(1000);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                try{
                    socket = new Socket(InetAddress.getByName(ip),PORT);
                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());
                    Log.i("네트워크설정","연결완료");
                }catch(Exception e){
                    e.printStackTrace();
                    Log.i("설정 오류","소켓 및 스트림 설정 오류 발생");
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            try{
                                if(startrealgps){
                                    os.flush();
                                    os.writeUTF("realstart");
                                    Log.i("서비스","전송완료");
                                    startrealgps = false;
                                }
                                if(stoprealgps){
                                    os.flush();
                                    os.writeUTF("realstop");
                                    stoprealgps = false;
                                }
                                Thread.sleep(1000);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            try{
                                Log.i("서비스","받기");
                                msg = is.readUTF();
                                s = msg.split(" ");

                                switch(s[0]){
                                    case "realtime":
                                        lat = Double.parseDouble(s[1]);
                                        lon = Double.parseDouble(s[2]);

                                        Log.i("서비스받는부분",Double.toString(lat));
                                        Log.i("서비스받는부분",Double.toString(lon));

                                        check = msg;
                                        break;
                                    case "gettimegps":
                                        GPSData.setla(Double.parseDouble(s[1]));
                                        GPSData.setlo(Double.parseDouble(s[2]));
                                        GPSData.setna(s[3]);

                                        datas.add(GPSData);

                                        gson = new GsonBuilder().create();
                                        Type listType = new TypeToken<ArrayList<GPSData>>(){}.getType();
                                        String json = gson.toJson(datas,listType);

                                        sp = getSharedPreferences("shared",MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("gps",json);
                                        editor.commit();
                                        break;
                                    case "geofenceenter":
                                        sendNotification(s[2],Integer.parseInt(s[1]));
                                        break;
                                    case "geofenceexit":
                                        sendNotification(s[2],Integer.parseInt(s[1]));
                                        break;
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        }).start();

        return mBinder;
    }

    public void setip(String ip){
        this.ip = ip;
        Log.i("IP 받은 값",ip);
    }

    private void sendNotification(String geofenceindex, int geofencet) {
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), ParentIndexActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(ParentIndexActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        if(geofencet == Geofence.GEOFENCE_TRANSITION_ENTER){
            builder.setColor(Color.BLUE)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                    .setContentTitle(geofenceindex)
                    .setContentText("안전지대 내부에 진입했습니다.")
                    .setContentIntent(notificationPendingIntent);
        }else if(geofencet== Geofence.GEOFENCE_TRANSITION_EXIT){
            builder.setColor(Color.RED)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                    .setContentTitle(geofenceindex)
                    .setContentText("안전지대를 이탈했습니다")
                    .setContentIntent(notificationPendingIntent);
        }

        // Define the notification settings.


        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    public LatLng getchildgps(){
        Log.i("서비스함수부분",Double.toString(lat));
        Log.i("서비스함수부분",Double.toString(lon));
        return new LatLng(lat,lon);

    }

    public void startgpsset(){
        startrealgps = true;
        Log.i("서비스","실제 스타트");
    }

    public void stopgpsset(){
        stoprealgps = true;
    }

    public String getmsg(){
        return check;
    }

}