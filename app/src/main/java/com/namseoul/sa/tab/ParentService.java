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
import java.io.IOException;
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

    ServerSocket serversocket;
    Socket socket;
    DataInputStream is;
    DataOutputStream os;
    String msg="";
    String strContact;
    String ip;

    private Gson gson;
    private SharedPreferences sp;
    private List<GPSData> datas;
    private GPSData GPSData;

    double lat,lon;

    boolean isConnected = true;
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
                try{
                    socket = new Socket(InetAddress.getByName(ip),PORT);
                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());
                }catch(Exception e){
                    e.printStackTrace();
                    Log.i("설정 오류","소켓 및 스트림 설정 오류 발생");
                }
                while (true){
                    try{
                        msg = is.readUTF();
                        String[] smsg = msg.split(" ");
                        if(smsg[0].equals("geofencewarring")){
                            sendNotification(smsg[1],smsg[2],Integer.parseInt(smsg[3]));
                        }else if(smsg[0].equals("gettimegps")){
                            GPSData.setla(Double.parseDouble(smsg[1]));
                            GPSData.setlo(Double.parseDouble(smsg[2]));
                            GPSData.setna(smsg[3]);

                            datas.add(GPSData);

                            gson = new GsonBuilder().create();
                            Type listType = new TypeToken<ArrayList<GPSData>>(){}.getType();
                            String json = gson.toJson(datas,listType);

                            sp = getSharedPreferences("shared",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("gps",json);
                            editor.commit();
                        }else if(smsg[0].equals("getrealps")){
                            lat = Double.parseDouble(smsg[1]);
                            lon = Double.parseDouble(smsg[2]);
                        }
                        Thread.sleep(1000);
                    }catch(Exception e){
                        //e.printStackTrace();
                    }
                }
            }
        }).start();

        return mBinder;
    }

    public void setip(String ip){
        this.ip = ip;
        Log.i("IP 받은 값",ip);
    }

    private void sendNotification(String geofenceindex, String notificationDetails, int geofencet) {
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
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

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
                    .setContentText(notificationDetails)
                    .setContentIntent(notificationPendingIntent);
        }else if(geofencet== Geofence.GEOFENCE_TRANSITION_EXIT){
            builder.setColor(Color.RED)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                    .setContentTitle(geofenceindex)
                    .setContentText(notificationDetails)
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

    public void sendrealtime(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    os.writeUTF("realtime");
                    os.flush();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public LatLng getchildgps(){
        return new LatLng(lat,lon);
    }

    public void sendstoptime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    os.writeUTF("realstop");
                    os.flush();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

}